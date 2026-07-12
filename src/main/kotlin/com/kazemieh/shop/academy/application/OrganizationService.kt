package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.OrganizationResponse
import com.kazemieh.shop.academy.api.dto.SeatResponse
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.OrganizationRepository
import com.kazemieh.shop.academy.persistence.OrganizationSeatRepository
import com.kazemieh.shop.academy.persistence.entity.EnrollmentEntity
import com.kazemieh.shop.academy.persistence.entity.OrganizationEntity
import com.kazemieh.shop.academy.persistence.entity.OrganizationSeatEntity
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * صندلی‌های سازمانی: یک سازمان تعدادی صندلیِ خالی برایِ یک دوره می‌خرد (buySeats)،
 * سپس هرکدام را با ایمیلِ یک کارمند پر می‌کند (assignSeat) که خودکار او را در دوره ثبت‌نام می‌کند.
 */
@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val seatRepository: OrganizationSeatRepository,
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(name: String, contactEmail: String?): OrganizationResponse =
        toResponse(organizationRepository.save(OrganizationEntity(name = name, contactEmail = contactEmail)))

    @Transactional(readOnly = true)
    fun list(): List<OrganizationResponse> = organizationRepository.findAll().map(::toResponse)

    @Transactional
    fun buySeats(organizationId: Long, courseId: Long, count: Int): List<SeatResponse> {
        val organization = organizationRepository.findById(organizationId)
            .orElseThrow { NotFoundException("Organization not found", ErrorCodes.ORGANIZATION_NOT_FOUND) }
        courseRepository.findById(courseId).orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
        val seats = (1..count).map {
            seatRepository.save(OrganizationSeatEntity(organizationId = organization.id, courseId = courseId))
        }
        return seats.map(::toSeatResponse)
    }

    @Transactional(readOnly = true)
    fun listSeats(organizationId: Long): List<SeatResponse> =
        seatRepository.findAllByOrganizationIdOrderByIdAsc(organizationId).map(::toSeatResponse)

    @Transactional
    fun assignSeat(organizationId: Long, courseId: Long, email: String): SeatResponse {
        val seat = seatRepository.findAllByOrganizationIdAndCourseIdAndAssignedUserIdIsNull(organizationId, courseId)
            .firstOrNull() ?: throw NotFoundException("No free seat available for this course", ErrorCodes.NO_SEATS_AVAILABLE)
        val user = userRepository.findByEmail(email) ?: throw NotFoundException("User not found", ErrorCodes.USER_NOT_FOUND)

        seat.assignedUserId = user.id
        seat.assignedEmail = email
        seat.assignedAt = OffsetDateTime.now()
        val saved = seatRepository.save(seat)

        if (!enrollmentRepository.existsByUserIdAndCourseId(user.id, courseId)) {
            val course = courseRepository.findById(courseId).orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
            enrollmentRepository.save(EnrollmentEntity(userId = user.id, course = course))
        }
        return toSeatResponse(saved)
    }

    private fun toResponse(e: OrganizationEntity) = OrganizationResponse(
        id = e.id, name = e.name, contactEmail = e.contactEmail, createdAt = e.createdAt?.toString()
    )

    private fun toSeatResponse(e: OrganizationSeatEntity) = SeatResponse(
        id = e.id, organizationId = e.organizationId, courseId = e.courseId,
        assignedUserId = e.assignedUserId, assignedEmail = e.assignedEmail,
        assignedAt = e.assignedAt?.toString()
    )
}
