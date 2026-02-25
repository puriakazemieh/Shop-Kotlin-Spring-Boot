package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal

data class VariantResponse(
    val id: Long,
    val sku: String,
    val price: BigDecimal,
    val compareAtPrice: BigDecimal?,
    val size: SizeResponse,
    val color: ColorResponse,
    val availableQty: Int,
    val isActive: Boolean,
)
