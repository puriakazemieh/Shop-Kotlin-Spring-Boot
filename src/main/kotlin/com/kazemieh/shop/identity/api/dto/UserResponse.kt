package com.kazemieh.shop.identity.api.dto

import java.time.OffsetDateTime

data class UserResponse(
    val id: Long,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val city: String?,
    val postalCode: Int?,
    val phone: String?,
    val role: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)
