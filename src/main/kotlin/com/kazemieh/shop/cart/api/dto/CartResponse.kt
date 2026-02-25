package com.kazemieh.shop.cart.api.dto

import java.math.BigDecimal

data class CartResponse(
    val items: List<CartItemResponse>,
    val subtotal: BigDecimal,
    val totalQty: Int
)