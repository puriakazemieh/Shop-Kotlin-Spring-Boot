package com.kazemieh.shop.order.api.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class CreateOrderRequest(
    val addressId: Long? = null,

    @field:NotEmpty
    @field:Valid
    val items: List<CreateOrderItemRequest>,

    val useWallet: Boolean = false
)
