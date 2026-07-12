package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal

data class PriceAlertRequest(
    val productId: Long,
    val variantId: Long,
    val targetPrice: BigDecimal
)

data class PriceAlertResponse(
    val variantId: Long,
    val targetPrice: BigDecimal,
    val subscribed: Boolean
)
