package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class ResetPasswordRequest(
    @field:NotBlank(message = "Token is required")
    val token: String,

    @field:NotBlank(message = "New password is required")
    @field:Length(min = 8, message = "Password must be at least 8 characters long")
    val newPassword: String
)
