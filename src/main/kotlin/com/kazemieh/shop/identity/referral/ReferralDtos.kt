package com.kazemieh.shop.identity.referral

import java.math.BigDecimal

data class ReferralInfoResponse(
    val code: String,
    val referredCount: Long,
    val totalEarned: BigDecimal
)
