package com.kazemieh.shop.cart.api.dto

import java.math.BigDecimal

data class CartItemResponse(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val savedForLater: Boolean,

    val productId: Long,
    val productTitle: String,
    val productSlug: String,
    val imageUrl: String?,

    val options: Map<String, String>,

    val price: BigDecimal,
    val compareAtPrice: BigDecimal?,
    val availableQty: Int,
    val isActive: Boolean,

    val lineTotal: BigDecimal
)