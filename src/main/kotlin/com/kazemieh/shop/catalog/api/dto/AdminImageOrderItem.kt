package com.kazemieh.shop.catalog.api.dto

import jakarta.validation.constraints.NotNull

data class AdminImageOrderItem(
    @field:NotNull val id: Long,
    @field:NotNull val sortOrder: Int
)