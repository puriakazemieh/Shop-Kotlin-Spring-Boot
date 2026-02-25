package com.kazemieh.shop.cart.api.dto

import jakarta.validation.constraints.Min

data class UpdateCartItemRequest(
    @field:Min(0) val qty: Int // 0 => remove
)
