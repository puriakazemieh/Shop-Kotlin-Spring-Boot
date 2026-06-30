package com.kazemieh.shop.catalog.api.dto

/** مشخصه‌ی محصول (کلید/مقدار) — مثلاً جنس: نخی. */
data class ProductAttributeDto(
    val name: String,
    val value: String
)
