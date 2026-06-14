package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal

data class VariantResponse(
    val id: Long,
    val sku: String,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val compareAtPrice: BigDecimal?,
    val options: Map<String, String>,
    val availableQty: Int,
    val isActive: Boolean,
)
