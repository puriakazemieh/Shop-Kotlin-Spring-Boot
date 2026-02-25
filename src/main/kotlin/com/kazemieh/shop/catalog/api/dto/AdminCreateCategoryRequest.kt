package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// ---------- Category ----------
data class AdminCreateCategoryRequest(
    @field:NotBlank @field:Size(max = 120) val name: String,
    @field:NotBlank @field:Size(max = 140) val slug: String,
    val parentId: Long? = null,
)







