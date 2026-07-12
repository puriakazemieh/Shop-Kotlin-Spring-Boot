package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email val email: String? = null,
    val mobile: String? = null,
    @field:NotBlank @field:Size(min = 8, max = 100) val password: String,
    val referralCode: String? = null,
)
