package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal

data class AdminVariantResponse(
    val id: Long,
    val productId: Long,
    val sizeId: Long,
    val sizeName: String,
    val colorId: Long,
    val colorName: String,
    val sku: String,
    val price: BigDecimal,
    val compareAtPrice: BigDecimal?,
    val isActive: Boolean,
    val inventory: AdminInventoryResponse?
)