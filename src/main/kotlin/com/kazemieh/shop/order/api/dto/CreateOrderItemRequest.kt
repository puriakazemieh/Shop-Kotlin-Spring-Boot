package com.kazemieh.shop.order.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateOrderItemRequest(
    @field:NotNull
    val variantId: Long,

    @field:Min(1)
    val qty: Int,
)


