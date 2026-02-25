package com.kazemieh.shop.customer.address.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAddressRequest(
    @field:NotBlank @field:Size(max = 255)
    val receiverName: String,

    @field:NotBlank @field:Size(max = 30)
    val receiverPhone: String,

    @field:NotBlank @field:Size(max = 80)
    val country: String = "IR",

    @field:NotBlank @field:Size(max = 120)
    val province: String,

    @field:NotBlank @field:Size(max = 120)
    val city: String,

    @field:NotBlank @field:Size(max = 255)
    val addressLine1: String,

    @field:Size(min = 1, max = 255)
    val addressLine2: String? = null,

    @field:Size(min = 1, max = 20)
    val postalCode: String? = null,

    val setAsDefault: Boolean = false,
)
