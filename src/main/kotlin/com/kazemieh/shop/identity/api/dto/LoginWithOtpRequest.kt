package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.NotBlank

data class LoginWithOtpRequest(
    @field:NotBlank val mobile: String,
    @field:NotBlank val otpCode: String,
)
