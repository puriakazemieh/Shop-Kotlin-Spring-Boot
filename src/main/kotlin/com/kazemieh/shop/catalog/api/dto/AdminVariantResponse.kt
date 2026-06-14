package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal

data class AdminVariantResponse(
    val id: Long,
    val productId: Long,
    val options: Map<String, String>,
    val sku: String,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val compareAtPrice: BigDecimal?,
    val isActive: Boolean,
    val inventory: AdminInventoryResponse?
)
