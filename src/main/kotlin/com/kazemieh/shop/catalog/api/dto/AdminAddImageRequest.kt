package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank

// ---------- Images ----------
data class AdminAddImageRequest(
    @field:NotBlank val url: String,
    val sortOrder: Int? = null
)
