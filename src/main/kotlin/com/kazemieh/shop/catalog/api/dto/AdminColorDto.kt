package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank

data class AdminCreateColorRequest(
    @field:NotBlank val name: String,
    val hex: String? = null
)

data class AdminUpdateColorRequest(
    val name: String? = null,
    val hex: String? = null
)

data class AdminColorResponse(
    val id: Long,
    val name: String,
    val hex: String?
)
