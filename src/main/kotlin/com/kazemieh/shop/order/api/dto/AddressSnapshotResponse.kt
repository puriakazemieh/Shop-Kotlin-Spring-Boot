package com.kazemieh.shop.order.api.dto

data class AddressSnapshotResponse(
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?,
)