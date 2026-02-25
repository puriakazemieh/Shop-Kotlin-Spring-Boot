package com.kazemieh.shop.cart.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddCartItemRequest(
    @field:NotNull val variantId: Long,
    @field:Min(1) val qty: Int
)
