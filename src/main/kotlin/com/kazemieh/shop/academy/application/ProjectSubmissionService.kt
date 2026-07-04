package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.ProjectSubmissionResponse
import com.kazemieh.shop.academy.api.dto.SubmitProjectRequest
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.ProjectSubmissionRepository
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionEntity
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionStatus
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ثبتِ پروژه‌ی پایانیِ کاربر برای یک دوره‌ی پروژه‌محور. هر کاربر برای هر دوره حداکثر یک
 * ثبتِ فعال دارد؛ ثبتِ دوباره (مثلاً پس از ردِ ادمین) وضعیت را به PENDING برمی‌گرداند.
 */
@Service
class ProjectSubmissionService(
    private val submissionRepository: ProjectSubmissionRepository,
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository
) {

    @Transactional
    fun submit(userId: Long, courseId: Long, req: SubmitProjectRequest): ProjectSubmissionResponse {
        courseRepository.findById(courseId).orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw ForbiddenException("Not enrolled in this course", ErrorCodes.NOT_ENROLLED)
        }
        val existing = submissionRepository.findByCourseIdAndUserId(courseId, userId)
        val submission = existing?.apply {
            fileUrl = req.fileUrl
            note = req.note
            status = ProjectSubmissionStatus.PENDING
            mentorFeedback = null
            reviewedAt = null
        } ?: ProjectSubmissionEntity(courseId = courseId, userId = userId, fileUrl = req.fileUrl, note = req.note)
        return submissionRepository.save(submission).toResponse()
    }

    @Transactional(readOnly = true)
    fun getMine(userId: Long, courseId: Long): ProjectSubmissionResponse? =
        submissionRepository.findByCourseIdAndUserId(courseId, userId)?.toResponse()

    private fun ProjectSubmissionEntity.toResponse() = ProjectSubmissionResponse(
        id = id, courseId = courseId, userId = userId, fileUrl = fileUrl, note = note,
        status = status.name, mentorFeedback = mentorFeedback,
        submittedAt = submittedAt.toString(), reviewedAt = reviewedAt?.toString()
    )
}
