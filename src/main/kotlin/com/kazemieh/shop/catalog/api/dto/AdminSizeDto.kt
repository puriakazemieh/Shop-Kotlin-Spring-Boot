package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank

data class AdminCreateSizeRequest(
    @field:NotBlank val name: String,
    val sortOrder: Int = 0
)

data class AdminUpdateSizeRequest(
    val name: String? = null,
    val sortOrder: Int? = null
)

data class AdminSizeResponse(
    val id: Long,
    val name: String,
    val sortOrder: Int
)
