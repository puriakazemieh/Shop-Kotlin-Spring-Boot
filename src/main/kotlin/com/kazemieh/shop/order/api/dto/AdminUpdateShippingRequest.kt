package com.kazemieh.shop.order.api.dto

import jakarta.validation.constraints.Size

data class AdminUpdateShippingRequest(
    @field:Size(max = 80)
    val shippingCarrier: String? = null,

    @field:Size(max = 120)
    val trackingCode: String? = null,

    val markShipped: Boolean = false
)