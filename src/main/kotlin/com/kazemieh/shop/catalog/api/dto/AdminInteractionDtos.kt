package com.kazemieh.shop.catalog.api.dto

import java.time.OffsetDateTime

data class AdminInteractionResponse(
    val id: Long,
    val productId: Long,
    val productTitle: String,
    val userId: Long,
    val userName: String,
    val content: String, // برای پرسش متن و برای نظر کامنت
    val rating: Int? = null, // فقط برای نظرات
    val isNew: Boolean,
    val createdAt: OffsetDateTime
)
