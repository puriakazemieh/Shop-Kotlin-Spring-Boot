package com.kazemieh.shop.customer.address.api.dto

import jakarta.validation.constraints.Size

data class UpdateAddressRequest(
    @field:Size(min = 1, max = 255)
    val receiverName: String? = null,

    @field:Size(min = 1, max = 30)
    val receiverPhone: String? = null,

    @field:Size(min = 1, max = 80)
    val country: String? = null,

    @field:Size(min = 1, max = 120)
    val province: String? = null,

    @field:Size(min = 1, max = 120)
    val city: String? = null,

    @field:Size(min = 1, max = 255)
    val addressLine1: String? = null,

    @field:Size(min = 1, max = 255)
    val addressLine2: String? = null,

    @field:Size(min = 1, max = 20)
    val postalCode: String? = null,
)
