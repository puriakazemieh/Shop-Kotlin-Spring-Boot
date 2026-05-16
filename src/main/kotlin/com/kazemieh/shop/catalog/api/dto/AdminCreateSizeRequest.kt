package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank

data class AdminCreateSizeRequest(
    @field:NotBlank val name: String,
    val sortOrder: Int = 0
)
