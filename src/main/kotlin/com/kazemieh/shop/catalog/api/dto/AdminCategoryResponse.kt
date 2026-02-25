package com.kazemieh.shop.catalog.api.dto

import java.time.OffsetDateTime

data class AdminCategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long?,
    val createdAt: OffsetDateTime?
)