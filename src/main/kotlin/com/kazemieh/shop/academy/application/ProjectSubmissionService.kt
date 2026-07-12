package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.PeerCommentResponse
import com.kazemieh.shop.academy.api.dto.ProjectSubmissionResponse
import com.kazemieh.shop.academy.api.dto.SubmitProjectRequest
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.ProjectPeerCommentRepository
import com.kazemieh.shop.academy.persistence.ProjectSubmissionRepository
import com.kazemieh.shop.academy.persistence.entity.ProjectPeerCommentEntity
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionEntity
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionStatus
import com.kazemieh.shop.identity.persistence.UserRepository
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
    private val enrollmentRepository: EnrollmentRepository,
    private val peerCommentRepository: ProjectPeerCommentRepository,
    private val userRepository: UserRepository
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

    /** پروژه‌هایِ تاییدشده‌ی دوره برایِ نقدِ همتایان — فقط دانشجویانِ ثبت‌نامیِ همان دوره می‌بینند. */
    @Transactional(readOnly = true)
    fun listApprovedForPeerReview(requesterId: Long, courseId: Long): List<ProjectSubmissionResponse> {
        if (!enrollmentRepository.existsByUserIdAndCourseId(requesterId, courseId)) {
            throw ForbiddenException("Not enrolled in this course", ErrorCodes.NOT_ENROLLED)
        }
        return submissionRepository.findAllByCourseIdAndStatusOrderBySubmittedAtDesc(courseId, ProjectSubmissionStatus.APPROVED)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun listPeerComments(submissionId: Long): List<PeerCommentResponse> {
        val comments = peerCommentRepository.findAllBySubmissionIdOrderByCreatedAtAsc(submissionId)
        val usersById = userRepository.findAllById(comments.map { it.userId }.distinct()).associateBy { it.id }
        return comments.map { c ->
            val user = usersById[c.userId]
            PeerCommentResponse(
                id = c.id,
                userId = c.userId,
                userName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "کاربر" },
                comment = c.comment,
                createdAt = c.createdAt?.toString()
            )
        }
    }

    @Transactional
    fun addPeerComment(userId: Long, submissionId: Long, comment: String): PeerCommentResponse {
        submissionRepository.findById(submissionId).orElseThrow { NotFoundException("Submission not found", ErrorCodes.PROJECT_SUBMISSION_NOT_FOUND) }
        val saved = peerCommentRepository.save(ProjectPeerCommentEntity(submissionId = submissionId, userId = userId, comment = comment))
        val user = userRepository.findById(userId).orElse(null)
        return PeerCommentResponse(
            id = saved.id, userId = userId,
            userName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "کاربر" },
            comment = saved.comment, createdAt = saved.createdAt?.toString()
        )
    }

    private fun ProjectSubmissionEntity.toResponse() = ProjectSubmissionResponse(
        id = id, courseId = courseId, userId = userId, fileUrl = fileUrl, note = note,
        status = status.name, mentorFeedback = mentorFeedback,
        submittedAt = submittedAt.toString(), reviewedAt = reviewedAt?.toString()
    )
}
