package com.kazemieh.shop.cart.api.dto

import jakarta.validation.constraints.Min

data class SetCartVariantQtyRequest(
    @field:Min(0)
    val qty: Int
)
