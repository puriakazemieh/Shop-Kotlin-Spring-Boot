package com.kazemieh.shop.cart.api.dto

import jakarta.validation.constraints.NotBlank

data class ApplyDiscountRequest(
    @NotBlank(message = "Discount code cannot be blank")
    val code: String
)
