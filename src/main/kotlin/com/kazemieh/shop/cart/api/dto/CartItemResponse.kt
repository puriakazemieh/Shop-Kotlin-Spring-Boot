package com.kazemieh.shop.cart.api.dto

import java.math.BigDecimal

data class CartItemResponse(
    val id: Long,
    val variantId: Long,
    val qty: Int,

    val productId: Long,
    val productTitle: String,
    val productSlug: String,
    val imageUrl: String?,

    val sizeName: String,
    val colorName: String,

    val price: BigDecimal,
    val compareAtPrice: BigDecimal?,
    val availableQty: Int,
    val isActive: Boolean,

    val lineTotal: BigDecimal
)
