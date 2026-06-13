package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class AdminUpdateProductRequest(
    val categoryId: Long? = null,
    @field:Size(min = 1, max = 255) val title: String? = null,
    @field:Size(min = 1, max = 280) val slug: String? = null,
    val description: String? = null,
    val basePrice: BigDecimal? = null,
    val discountedPrice: BigDecimal? = null,
    val isActive: Boolean? = null,
)
