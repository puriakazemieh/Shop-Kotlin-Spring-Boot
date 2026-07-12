package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.AdminReviewSwitchRequest
import com.kazemieh.shop.clinic.api.dto.AdminSwitchRequestResponse
import com.kazemieh.shop.clinic.api.dto.SwitchRequestRequest
import com.kazemieh.shop.clinic.api.dto.SwitchRequestResponse
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.TherapistSwitchRequestRepository
import com.kazemieh.shop.clinic.persistence.entity.SwitchRequestStatus
import com.kazemieh.shop.clinic.persistence.entity.TherapistSwitchRequestEntity
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/** درخواستِ تعویضِ درمانگر — مراجع می‌خواهد به درمانگرِ دیگری منتقل شود؛ ادمین بررسی می‌کند. */
@Service
class TherapistSwitchService(
    private val repository: TherapistSwitchRequestRepository,
    private val therapistRepository: TherapistRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun request(userId: Long, req: SwitchRequestRequest): SwitchRequestResponse {
        therapistRepository.findById(req.fromTherapistId).orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
        req.toTherapistId?.let {
            therapistRepository.findById(it).orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
        }
        if (repository.existsByUserIdAndFromTherapistIdAndStatus(userId, req.fromTherapistId, SwitchRequestStatus.PENDING)) {
            throw ConflictException("Switch request already pending", ErrorCodes.SWITCH_REQUEST_NOT_FOUND)
        }
        val saved = repository.save(
            TherapistSwitchRequestEntity(
                userId = userId, fromTherapistId = req.fromTherapistId,
                toTherapistId = req.toTherapistId, reason = req.reason?.trim()?.ifBlank { null }
            )
        )
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<SwitchRequestResponse> =
        repository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun adminList(): List<AdminSwitchRequestResponse> =
        repository.findAllByOrderByCreatedAtDesc().map { req ->
            val userName = userRepository.findById(req.userId)
                .map { listOfNotNull(it.firstName, it.lastName).joinToString(" ").ifBlank { null } }
                .orElse(null)
            req.toAdminResponse(userName)
        }

    @Transactional
    fun adminReview(id: Long, approve: Boolean, adminNote: String?): AdminSwitchRequestResponse {
        val req = repository.findById(id).orElseThrow { NotFoundException("Switch request not found", ErrorCodes.SWITCH_REQUEST_NOT_FOUND) }
        req.status = if (approve) SwitchRequestStatus.APPROVED else SwitchRequestStatus.REJECTED
        req.adminNote = adminNote
        req.resolvedAt = OffsetDateTime.now()
        val saved = repository.save(req)
        val userName = userRepository.findById(saved.userId)
            .map { listOfNotNull(it.firstName, it.lastName).joinToString(" ").ifBlank { null } }
            .orElse(null)
        return saved.toAdminResponse(userName)
    }

    private fun TherapistSwitchRequestEntity.toResponse() = SwitchRequestResponse(
        id = id, fromTherapistId = fromTherapistId,
        fromTherapistName = therapistRepository.findById(fromTherapistId).map { it.name }.orElse(""),
        toTherapistId = toTherapistId,
        toTherapistName = toTherapistId?.let { therapistRepository.findById(it).map { t -> t.name }.orElse(null) },
        reason = reason, status = status.name, adminNote = adminNote, createdAt = createdAt?.toString()
    )

    private fun TherapistSwitchRequestEntity.toAdminResponse(userName: String?) = AdminSwitchRequestResponse(
        id = id, userId = userId, userName = userName, fromTherapistId = fromTherapistId,
        fromTherapistName = therapistRepository.findById(fromTherapistId).map { it.name }.orElse(""),
        toTherapistId = toTherapistId,
        toTherapistName = toTherapistId?.let { therapistRepository.findById(it).map { t -> t.name }.orElse(null) },
        reason = reason, status = status.name, adminNote = adminNote, createdAt = createdAt?.toString()
    )
}
