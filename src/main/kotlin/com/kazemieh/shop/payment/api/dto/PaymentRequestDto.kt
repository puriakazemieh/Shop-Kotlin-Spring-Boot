package com.kazemieh.shop.payment.api.dto

data class PaymentRequestDto(
    val amountInToman: Long,
    val orderId: String
)