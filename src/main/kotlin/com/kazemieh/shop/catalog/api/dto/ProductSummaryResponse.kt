package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal

data class ProductSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String?,
    val minPrice: BigDecimal?,
    val maxPrice: BigDecimal?,
    val inStock: Boolean,
    val categoryId: Long?,
    val categoryName: String?,
)
