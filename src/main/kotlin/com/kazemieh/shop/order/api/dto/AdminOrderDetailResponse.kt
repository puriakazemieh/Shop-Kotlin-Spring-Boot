package com.kazemieh.shop.order.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class AdminOrderDetailResponse(
    val id: Long,
    val userId: Long,
    val identity: String,
    val status: String,
    val subtotalPrice: BigDecimal,
    val shippingPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    val addressSnapshot: Any,
    val items: List<AdminOrderItemResponse>
)
