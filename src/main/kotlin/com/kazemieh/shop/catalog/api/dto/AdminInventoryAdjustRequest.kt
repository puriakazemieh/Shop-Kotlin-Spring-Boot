package com.kazemieh.shop.catalog.api.dto

data class AdminInventoryAdjustRequest(
    val delta: Int,
    val version: Int? = null
)