package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.Min

data class AdminInventorySetRequest(
    @field:Min(0) val onHand: Int,
    val version: Int? = null
)