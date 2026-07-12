package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.AdminCourseRefundRequestResponse
import com.kazemieh.shop.academy.api.dto.CourseRefundRequestResponse
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.CourseRefundRequestRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.LessonProgressRepository
import com.kazemieh.shop.academy.persistence.entity.CourseRefundRequestEntity
import com.kazemieh.shop.academy.persistence.entity.RefundRequestStatus
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import com.kazemieh.shop.wallet.application.WalletService
import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * گارانتیِ بازگشتِ وجهِ دوره‌های دیجیتال: تا ۷ روز پس از ثبت‌نام و زیرِ ۲۰٪ پیشرفت،
 * دانشجو می‌تواند درخواستِ بازگشتِ وجه بدهد؛ با تأییدِ ادمین، مبلغ به کیف‌پول برمی‌گردد
 * و ثبت‌نام لغو می‌شود.
 */
@Service
class CourseRefundService(
    private val refundRequestRepository: CourseRefundRequestRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository,
    private val progressRepository: LessonProgressRepository,
    private val userRepository: UserRepository,
    private val walletService: WalletService
) {
    private val guaranteeDays = 7L
    private val maxProgressPercent = 20

    @Transactional
    fun requestRefund(userId: Long, courseId: Long, reason: String?): CourseRefundRequestResponse {
        val course = courseRepository.findById(courseId).orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
        val enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
            ?: throw ForbiddenException("Not enrolled in this course", ErrorCodes.NOT_ENROLLED)

        val enrolledAt = enrollment.enrolledAt ?: OffsetDateTime.now()
        if (enrolledAt.isBefore(OffsetDateTime.now().minusDays(guaranteeDays))) {
            throw ForbiddenException("Refund guarantee window has passed", ErrorCodes.REFUND_NOT_ELIGIBLE)
        }
        val total = course.sections.sumOf { it.lessons.size }
        val completed = progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId).toInt()
        val percent = if (total == 0) 0 else (completed * 100) / total
        if (percent > maxProgressPercent) {
            throw ForbiddenException("Too much progress made for a refund", ErrorCodes.REFUND_NOT_ELIGIBLE)
        }
        if (refundRequestRepository.existsByUserIdAndCourseIdAndStatusNot(userId, courseId, RefundRequestStatus.REJECTED)) {
            throw ConflictException("Refund already requested for this course", ErrorCodes.REFUND_ALREADY_REQUESTED)
        }

        val amount = course.discountedPrice ?: course.price
        val saved = refundRequestRepository.save(
            CourseRefundRequestEntity(courseId = courseId, userId = userId, amount = amount, reason = reason)
        )
        return toResponse(saved, course.title)
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<CourseRefundRequestResponse> =
        refundRequestRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { req ->
            val title = courseRepository.findById(req.courseId).map { it.title }.orElse("")
            toResponse(req, title)
        }

    @Transactional(readOnly = true)
    fun adminList(): List<AdminCourseRefundRequestResponse> =
        refundRequestRepository.findAllByOrderByCreatedAtDesc().map { req ->
            val title = courseRepository.findById(req.courseId).map { it.title }.orElse("")
            val userName = userRepository.findById(req.userId)
                .map { listOfNotNull(it.firstName, it.lastName).joinToString(" ").ifBlank { null } }
                .orElse(null)
            toAdminResponse(req, title, userName)
        }

    @Transactional
    fun adminReview(id: Long, approve: Boolean, adminNote: String?): AdminCourseRefundRequestResponse {
        val req = refundRequestRepository.findById(id).orElseThrow { NotFoundException("Refund request not found", ErrorCodes.REFUND_REQUEST_NOT_FOUND) }
        req.adminNote = adminNote
        req.resolvedAt = OffsetDateTime.now()
        if (approve) {
            req.status = RefundRequestStatus.APPROVED
            walletService.addTransaction(
                req.userId, req.amount, TransactionType.REFUND,
                "بازگشتِ وجهِ دوره", req.courseId.toString()
            )
            enrollmentRepository.deleteByUserIdAndCourseId(req.userId, req.courseId)
        } else {
            req.status = RefundRequestStatus.REJECTED
        }
        val saved = refundRequestRepository.save(req)
        val title = courseRepository.findById(saved.courseId).map { it.title }.orElse("")
        val userName = userRepository.findById(saved.userId)
            .map { listOfNotNull(it.firstName, it.lastName).joinToString(" ").ifBlank { null } }
            .orElse(null)
        return toAdminResponse(saved, title, userName)
    }

    private fun toResponse(e: CourseRefundRequestEntity, courseTitle: String) = CourseRefundRequestResponse(
        id = e.id, courseId = e.courseId, courseTitle = courseTitle, amount = e.amount,
        reason = e.reason, status = e.status.name, adminNote = e.adminNote,
        createdAt = e.createdAt?.toString(), resolvedAt = e.resolvedAt?.toString()
    )

    private fun toAdminResponse(e: CourseRefundRequestEntity, courseTitle: String, userName: String?) = AdminCourseRefundRequestResponse(
        id = e.id, courseId = e.courseId, courseTitle = courseTitle, userId = e.userId, userName = userName,
        amount = e.amount, reason = e.reason, status = e.status.name, adminNote = e.adminNote,
        createdAt = e.createdAt?.toString(), resolvedAt = e.resolvedAt?.toString()
    )
}
