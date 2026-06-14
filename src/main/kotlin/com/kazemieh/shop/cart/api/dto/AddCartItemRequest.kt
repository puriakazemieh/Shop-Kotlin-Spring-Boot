package com.kazemieh.shop.cart.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddCartItemRequest(
    val variantId: Long? = null,
    val productId: Long? = null,
    @field:Min(1) val qty: Int
)
