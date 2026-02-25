package com.kazemieh.shop.catalog.api.dto

data class AdminProductDetailResponse(
    val product: AdminProductResponse,
    val images: List<AdminProductImageResponse>,
    val variants: List<AdminVariantResponse>
)