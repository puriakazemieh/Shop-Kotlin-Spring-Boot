package com.kazemieh.shop.payment.application

data class ZarinPalVerificationResponse(
    val isSuccess: Boolean,
    val refId: String? = null
)