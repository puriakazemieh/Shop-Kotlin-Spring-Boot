package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

// ---------- Product ----------
data class AdminCreateProductRequest(
    val categoryId: Long? = null,
    @field:NotBlank @field:Size(max = 255) val title: String,
    @field:NotBlank @field:Size(max = 280) val slug: String,
    val description: String? = null,
    val basePrice: BigDecimal? = null,
    val isActive: Boolean = true,
    val variants: List<AdminCreateVariantRequest>? = null
)
