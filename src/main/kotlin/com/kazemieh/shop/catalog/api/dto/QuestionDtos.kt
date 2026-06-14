package com.kazemieh.shop.catalog.api.dto

import java.time.OffsetDateTime

data class CreateQuestionRequest(
    val productId: Long,
    val content: String,
    val parentId: Long? = null
)

data class UpdateQuestionRequest(
    val content: String
)

data class QuestionResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val replies: List<QuestionResponse>,
    val createdAt: OffsetDateTime
)
