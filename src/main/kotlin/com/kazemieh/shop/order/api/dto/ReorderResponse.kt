package com.kazemieh.shop.order.api.dto

import com.kazemieh.shop.cart.api.dto.CartResponse

data class ReorderResponse(
    val cart: CartResponse,
    /** عنوانِ آیتم‌هایی که به‌دلیلِ غیرفعال‌بودن یا نبودِ موجودی به سبد اضافه نشدند. */
    val skippedTitles: List<String>
)
