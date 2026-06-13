package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.NotBlank

data class SendLoginOtpRequest(
    @field:NotBlank val mobile: String,
)
