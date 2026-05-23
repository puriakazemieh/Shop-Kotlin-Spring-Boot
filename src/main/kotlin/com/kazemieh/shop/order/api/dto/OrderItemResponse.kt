package com.kazemieh.shop.order.api.dto

import java.math.BigDecimal

data class OrderItemResponse(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val unitPrice: BigDecimal,
    val title: String,
    val options: Map<String, String>
)
