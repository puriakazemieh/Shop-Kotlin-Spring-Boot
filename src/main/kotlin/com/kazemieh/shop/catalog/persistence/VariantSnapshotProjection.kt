package com.kazemieh.shop.catalog.persistence

import java.math.BigDecimal

interface VariantSnapshotProjection {
    fun getVariantId(): Long
    fun getPrice(): BigDecimal
    fun getTitle(): String
    fun getSizeName(): String
    fun getColorName(): String
    fun getIsActive(): Boolean
}