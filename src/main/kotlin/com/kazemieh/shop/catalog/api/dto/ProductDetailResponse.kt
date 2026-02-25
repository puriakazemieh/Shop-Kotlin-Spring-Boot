package com.kazemieh.shop.catalog.api.dto

import java.time.OffsetDateTime

data class ProductDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val images: List<ProductImageResponse>,
    val variants: List<VariantResponse>,
    val createdAt: OffsetDateTime?,
)