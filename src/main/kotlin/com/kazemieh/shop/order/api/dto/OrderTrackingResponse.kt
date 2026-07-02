package com.kazemieh.shop.order.api.dto

import com.kazemieh.shop.order.persistence.entity.OrderStatus
import java.time.Instant

data class OrderTrackingResponse(
    val id: Int,
    val status: OrderStatus,
    val trackingCode: String?,
    val orderedAt: Instant,
    val shippedAt: Instant?,
    val history: List<OrderStatusHistoryItem> = emptyList()
)

data class OrderStatusHistoryItem(
    val status: OrderStatus,
    val at: Instant
)
