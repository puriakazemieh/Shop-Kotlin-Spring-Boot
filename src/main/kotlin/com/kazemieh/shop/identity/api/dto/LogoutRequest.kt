package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank val refreshToken: String,
)