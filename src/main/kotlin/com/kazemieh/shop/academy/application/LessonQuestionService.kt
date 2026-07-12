package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.CreateLessonQuestionRequest
import com.kazemieh.shop.academy.api.dto.LessonQuestionResponse
import com.kazemieh.shop.academy.persistence.LessonQuestionRepository
import com.kazemieh.shop.academy.persistence.entity.LessonQuestionEntity
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LessonQuestionService(
    private val lessonQuestionRepository: LessonQuestionRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun list(lessonId: Long): List<LessonQuestionResponse> {
        val questions = lessonQuestionRepository.findAllByLessonIdOrderByCreatedAtAsc(lessonId)
        val userIds = questions.map { it.userId }.distinct()
        val usersById = userRepository.findAllById(userIds).associateBy { it.id }
        return questions.map { q ->
            val user = usersById[q.userId]
            LessonQuestionResponse(
                id = q.id,
                userId = q.userId,
                userName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "کاربر" },
                content = q.content,
                parentId = q.parentId,
                createdAt = q.createdAt?.toString()
            )
        }
    }

    @Transactional
    fun create(userId: Long, lessonId: Long, req: CreateLessonQuestionRequest): LessonQuestionResponse {
        val saved = lessonQuestionRepository.save(
            LessonQuestionEntity(lessonId = lessonId, userId = userId, content = req.content, parentId = req.parentId)
        )
        val user = userRepository.findById(userId).orElse(null)
        return LessonQuestionResponse(
            id = saved.id,
            userId = userId,
            userName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "کاربر" },
            content = saved.content,
            parentId = saved.parentId,
            createdAt = saved.createdAt?.toString()
        )
    }
}
