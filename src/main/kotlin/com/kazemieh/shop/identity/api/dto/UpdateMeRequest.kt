package com.kazemieh.shop.identity.api.dto

import jakarta.validation.constraints.Size

data class UpdateMeRequest(
    @field:Size(max = 255)
    val fullName: String? = null,

    @field:Size(max = 30)
    val phone: String? = null,
)
