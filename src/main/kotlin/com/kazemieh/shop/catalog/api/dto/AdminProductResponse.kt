package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class AdminProductResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val categoryId: Long?,
    val isActive: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    var basePrice: BigDecimal? = null,
    var discountedPrice: BigDecimal? = null,
    var description: String? = null,
    var brand: String? = null,
    var attributes: List<ProductAttributeDto> = emptyList(),
)
