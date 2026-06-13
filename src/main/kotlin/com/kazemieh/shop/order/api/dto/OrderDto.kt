package com.kazemieh.shop.order.api.dto

import com.kazemieh.shop.order.persistence.OrderStatus
import java.time.Instant

data class OrderTrackingView(
    val id: Int,
    val status: OrderStatus,
    val trackingCode: String?,
    val orderedAt: Instant,
    val shippedAt: Instant?
)
