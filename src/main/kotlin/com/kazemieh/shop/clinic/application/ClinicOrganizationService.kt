package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.academy.persistence.OrganizationRepository
import com.kazemieh.shop.clinic.api.dto.ClinicSeatResponse
import com.kazemieh.shop.clinic.persistence.ClinicOrganizationSeatRepository
import com.kazemieh.shop.clinic.persistence.SessionCreditRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.ClinicOrganizationSeatEntity
import com.kazemieh.shop.clinic.persistence.entity.SessionCreditEntity
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * بسته‌ی مشاوره‌ی سازمانی: سازمان (از فازِ W — academy.OrganizationEntity) صندلیِ جلسه
 * برایِ یک درمانگرِ مشخص می‌خرد؛ با اختصاصِ صندلی به ایمیلِ کارمند، اعتبارِ جلسه به او اعطا می‌شود.
 */
@Service
class ClinicOrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val seatRepository: ClinicOrganizationSeatRepository,
    private val therapistRepository: TherapistRepository,
    private val creditRepository: SessionCreditRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun buySeats(organizationId: Long, therapistId: Long, sessionCount: Int, count: Int): List<ClinicSeatResponse> {
        organizationRepository.findById(organizationId)
            .orElseThrow { NotFoundException("Organization not found", ErrorCodes.ORGANIZATION_NOT_FOUND) }
        therapistRepository.findById(therapistId).orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
        val seats = (1..count).map {
            seatRepository.save(
                ClinicOrganizationSeatEntity(organizationId = organizationId, therapistId = therapistId, sessionCount = sessionCount.coerceAtLeast(1))
            )
        }
        return seats.map(::toResponse)
    }

    @Transactional(readOnly = true)
    fun listSeats(organizationId: Long): List<ClinicSeatResponse> =
        seatRepository.findAllByOrganizationIdOrderByIdAsc(organizationId).map(::toResponse)

    @Transactional
    fun assignSeat(organizationId: Long, therapistId: Long, email: String): ClinicSeatResponse {
        val seat = seatRepository.findAllByOrganizationIdAndTherapistIdAndAssignedUserIdIsNull(organizationId, therapistId)
            .firstOrNull() ?: throw NotFoundException("No free seat available for this therapist", ErrorCodes.NO_CLINIC_SEATS_AVAILABLE)
        val user = userRepository.findByEmail(email) ?: throw NotFoundException("User not found", ErrorCodes.USER_NOT_FOUND)

        seat.assignedUserId = user.id
        seat.assignedEmail = email
        seat.assignedAt = OffsetDateTime.now()
        val saved = seatRepository.save(seat)

        val credit = creditRepository.findByUserIdAndTherapistIdForUpdate(user.id, therapistId)
            ?: SessionCreditEntity(userId = user.id, therapistId = therapistId, remaining = 0)
        credit.remaining += seat.sessionCount
        creditRepository.save(credit)

        return toResponse(saved)
    }

    private fun toResponse(e: ClinicOrganizationSeatEntity) = ClinicSeatResponse(
        id = e.id, organizationId = e.organizationId, therapistId = e.therapistId, sessionCount = e.sessionCount,
        assignedUserId = e.assignedUserId, assignedEmail = e.assignedEmail, assignedAt = e.assignedAt?.toString()
    )
}
