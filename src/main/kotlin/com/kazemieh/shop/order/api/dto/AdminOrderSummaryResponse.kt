package com.kazemieh.shop.order.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class AdminOrderSummaryResponse(
    val id: Long,
    val userId: Long,
    val userEmail: String,
    val status: String,
    val totalPrice: BigDecimal,
    val createdAt: OffsetDateTime?
)
