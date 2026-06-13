package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.Email

data class ForgotPasswordRequest(
    @field:Email val email: String? = null,
    val mobile: String? = null
)
