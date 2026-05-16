package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank

data class AdminCreateColorRequest(
    @field:NotBlank val name: String,
    val hex: String? = null
)
