package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class AdminUpdateVariantRequest(
    @field:Size(min = 1, max = 80) val sku: String? = null,
    @field:DecimalMin("0.0") val price: BigDecimal? = null,
    @field:DecimalMin("0.0") val discountedPrice: BigDecimal? = null,
    @field:DecimalMin("0.0") val compareAtPrice: BigDecimal? = null,
    @field:Valid @field:Size(min = 1) val options: List<OptionPair>? = null,
    val isActive: Boolean? = null
)