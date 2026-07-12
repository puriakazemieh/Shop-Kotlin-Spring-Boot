package com.kazemieh.shop.cart.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class CartResponse(
    val items: List<CartItemResponse>,
    val savedForLater: List<CartItemResponse>,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val total: BigDecimal,
    val totalQty: Int,
    val appliedDiscountCode: String? = null,
    /** آخرین تغییرِ سبد — کلاینت برایِ نمایشِ بنرِ «یادآوریِ سبدِ رها‌شده» از این استفاده می‌کند. */
    val updatedAt: OffsetDateTime? = null
)
