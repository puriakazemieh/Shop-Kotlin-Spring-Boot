package com.kazemieh.shop.customer.address.api.dto

import java.time.OffsetDateTime

data class AddressResponse(
    val id: Long,
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?,
    val isDefault: Boolean,
    val createdAt: OffsetDateTime?,
)