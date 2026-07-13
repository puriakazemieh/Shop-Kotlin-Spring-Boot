package com.kazemieh.shop.academy.courserequest

import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseRequestService(
    private val requestRepository: CourseRequestRepository,
    private val voteRepository: CourseRequestVoteRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(userId: Long, req: CreateCourseRequestRequest): CourseRequestResponse {
        val title = req.title.trim()
        val name = userRepository.findById(userId).orElse(null)?.let {
            listOfNotNull(it.firstName, it.lastName).joinToString(" ").trim().ifBlank { null }
        }
        val entity = CourseRequestEntity(
            title = title,
            description = req.description?.trim()?.ifBlank { null },
            requesterUserId = userId,
            requesterName = name ?: "کاربر"
        )
        return requestRepository.save(entity).toResponse(liked = false)
    }

    @Transactional(readOnly = true)
    fun listPublic(userId: Long?): List<CourseRequestResponse> {
        val likedIds = if (userId != null) {
            voteRepository.findAllByUserId(userId).map { it.requestId }.toSet()
        } else emptySet()
        return requestRepository.findAllByOrderByLikeCountDescCreatedAtDesc()
            .map { it.toResponse(liked = it.id in likedIds) }
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<CourseRequestResponse> {
        val likedIds = voteRepository.findAllByUserId(userId).map { it.requestId }.toSet()
        return requestRepository.findAllByRequesterUserIdOrderByCreatedAtDesc(userId)
            .map { it.toResponse(liked = it.id in likedIds) }
    }

    @Transactional
    fun toggleLike(userId: Long, requestId: Long): ToggleLikeResponse {
        val request = requestRepository.findById(requestId).orElseThrow {
            NotFoundException("Course request not found", ErrorCodes.COURSE_REQUEST_NOT_FOUND)
        }
        val existing = voteRepository.findByRequestIdAndUserId(requestId, userId)
        val liked: Boolean
        if (existing != null) {
            voteRepository.delete(existing)
            request.likeCount = (request.likeCount - 1).coerceAtLeast(0)
            liked = false
        } else {
            voteRepository.save(CourseRequestVoteEntity(requestId = requestId, userId = userId))
            request.likeCount += 1
            liked = true
        }
        requestRepository.save(request)
        return ToggleLikeResponse(liked = liked, likeCount = request.likeCount)
    }

    // ---- ادمین ----
    @Transactional(readOnly = true)
    fun listAll(): List<CourseRequestResponse> =
        requestRepository.findAllByOrderByLikeCountDescCreatedAtDesc().map { it.toResponse(liked = false) }

    @Transactional
    fun delete(requestId: Long) {
        val request = requestRepository.findById(requestId).orElseThrow {
            NotFoundException("Course request not found", ErrorCodes.COURSE_REQUEST_NOT_FOUND)
        }
        voteRepository.deleteAllByRequestId(requestId)
        requestRepository.delete(request)
    }

    @Transactional
    fun setFulfilled(requestId: Long, fulfilled: Boolean): CourseRequestResponse {
        val request = requestRepository.findById(requestId).orElseThrow {
            NotFoundException("Course request not found", ErrorCodes.COURSE_REQUEST_NOT_FOUND)
        }
        request.fulfilled = fulfilled
        return requestRepository.save(request).toResponse(liked = false)
    }

    private fun CourseRequestEntity.toResponse(liked: Boolean) = CourseRequestResponse(
        id = id,
        title = title,
        description = description,
        requesterName = requesterName,
        likeCount = likeCount,
        liked = liked,
        fulfilled = fulfilled,
        createdAt = createdAt?.toString()
    )
}
