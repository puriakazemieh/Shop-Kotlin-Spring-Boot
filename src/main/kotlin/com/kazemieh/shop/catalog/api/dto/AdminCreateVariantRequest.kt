package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

// ---------- Variant + Inventory ----------
data class AdminCreateVariantRequest(
    @field:NotNull val sizeId: Long,
    @field:NotNull val colorId: Long,
    @field:NotBlank @field:Size(max = 80) val sku: String,
    @field:NotNull @field:DecimalMin("0.0") val price: BigDecimal,
    @field:DecimalMin("0.0") val compareAtPrice: BigDecimal? = null,
    val isActive: Boolean = true,

    // inventory initial
    @field:Min(0) val initialOnHand: Int = 0
)