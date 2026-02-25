package com.kazemieh.shop.catalog.api.dto

data class CategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long?,
    val children: List<CategoryResponse> = emptyList(),
)