package com.kazemieh.shop.cart.api.dto

import java.math.BigDecimal

data class CartResponse(
    val items: List<CartItemResponse>,
    val savedForLater: List<CartItemResponse>,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val total: BigDecimal,
    val totalQty: Int,
    val appliedDiscountCode: String? = null
)
