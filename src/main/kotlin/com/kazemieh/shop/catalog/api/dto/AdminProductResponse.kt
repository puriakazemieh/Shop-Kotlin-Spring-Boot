package com.kazemieh.shop.catalog.api.dto

import java.time.OffsetDateTime

data class AdminProductResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val categoryId: Long?,
    val isActive: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)