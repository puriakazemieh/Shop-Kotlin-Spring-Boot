package com.kazemieh.shop.academy.api.dto

data class CreateLessonQuestionRequest(
    val content: String,
    val parentId: Long? = null
)

data class LessonQuestionResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val parentId: Long?,
    val createdAt: String?
)
