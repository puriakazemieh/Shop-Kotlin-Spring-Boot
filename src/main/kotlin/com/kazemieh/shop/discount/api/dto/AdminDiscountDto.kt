package com.kazemieh.shop.discount.api.dto

import com.kazemieh.shop.discount.persistence.entity.DiscountType
import java.math.BigDecimal
import java.time.OffsetDateTime

data class CreateDiscountRequest(
    val code: String,
    val type: DiscountType,
    val value: BigDecimal,
    val maxDiscountAmount: BigDecimal? = null,
    val minOrderAmount: BigDecimal? = null,
    val startDate: OffsetDateTime? = null,
    val endDate: OffsetDateTime? = null,
    val usageLimit: Int? = null,
    val isActive: Boolean = true
)

data class UpdateDiscountRequest(
    val code: String? = null,
    val type: DiscountType? = null,
    val value: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    val minOrderAmount: BigDecimal? = null,
    val startDate: OffsetDateTime? = null,
    val endDate: OffsetDateTime? = null,
    val usageLimit: Int? = null,
    val isActive: Boolean? = null
)

data class DiscountResponse(
    val id: Long,
    val code: String,
    val type: DiscountType,
    val value: BigDecimal,
    val maxDiscountAmount: BigDecimal?,
    val minOrderAmount: BigDecimal?,
    val startDate: OffsetDateTime?,
    val endDate: OffsetDateTime?,
    val usageLimit: Int?,
    val usageCount: Int,
    val isActive: Boolean
)
