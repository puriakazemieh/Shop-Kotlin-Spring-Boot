package com.kazemieh.shop.order.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class OrderDetailResponse(
    val id: Long,
    val status: String,
    val subtotalPrice: BigDecimal,
    val shippingPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val walletPaidAmount: BigDecimal,
    val gatewayPaidAmount: BigDecimal,
    val createdAt: OffsetDateTime?,
    val address: AddressSnapshotResponse,
    val items: List<OrderItemResponse>,
    val isGift: Boolean = false,
    val giftMessage: String? = null,
)