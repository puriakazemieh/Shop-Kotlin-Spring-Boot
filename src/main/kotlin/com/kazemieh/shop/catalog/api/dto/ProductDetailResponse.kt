package com.kazemieh.shop.catalog.api.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class ProductDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val basePrice: BigDecimal? = null,
    val discountedPrice: BigDecimal? = null,
    val categoryId: Long?,
    val categoryName: String?,
    val images: List<ProductImageResponse>,
    val videos: List<ProductVideoResponse>,
    val variants: List<VariantResponse>,
    val createdAt: OffsetDateTime?,
    val isFavorite: Boolean = false,
)
