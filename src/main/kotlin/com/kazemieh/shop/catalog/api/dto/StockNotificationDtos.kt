package com.kazemieh.shop.catalog.api.dto

data class StockNotificationRequest(
    val productId: Long,
    val variantId: Long
)

data class StockNotificationResponse(
    val variantId: Long,
    val subscribed: Boolean
)
