package com.kazemieh.shop.order.api.dto

import java.math.BigDecimal

data class AdminOrderItemResponse(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val unitPriceSnapshot: BigDecimal,
    val titleSnapshot: String,
    val optionsSnapshot: Map<String, String>?
)
