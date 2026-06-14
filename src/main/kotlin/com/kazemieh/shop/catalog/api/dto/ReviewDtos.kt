package com.kazemieh.shop.catalog.api.dto

import java.time.OffsetDateTime

data class CreateReviewRequest(
    val productId: Long,
    val rating: Int?,
    val comment: String,
    val parentId: Long? = null
)

data class UpdateReviewRequest(
    val rating: Int?,
    val comment: String
)

data class ReviewResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val rating: Int?,
    val comment: String,
    val replies: List<ReviewResponse>,
    val createdAt: OffsetDateTime
)
