package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(max = 255)
    val fullName: String?,

    @field:Size(max = 30)
    val phone: String?
)