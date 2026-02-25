package com.kazemieh.shop.order.api.dto

import jakarta.validation.constraints.NotBlank

data class AdminUpdateOrderStatusRequest(
    @field:NotBlank
    val status: String
)