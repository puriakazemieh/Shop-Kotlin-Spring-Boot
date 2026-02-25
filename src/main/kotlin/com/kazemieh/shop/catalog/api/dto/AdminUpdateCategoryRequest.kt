package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.Size

data class AdminUpdateCategoryRequest(
    @field:Size(min = 1, max = 120) val name: String? = null,
    @field:Size(min = 1, max = 140) val slug: String? = null,
    val parentId: Long? = null,
)
