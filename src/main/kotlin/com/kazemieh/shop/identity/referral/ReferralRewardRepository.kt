package com.kazemieh.shop.identity.referral

import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

interface ReferralRewardRepository : JpaRepository<ReferralRewardEntity, Long> {
    fun existsByOrderId(orderId: Long): Boolean
    fun findAllByReferrerIdOrderByCreatedAtDesc(referrerId: Long): List<ReferralRewardEntity>

    fun sumAmountByReferrerId(referrerId: Long): BigDecimal? =
        findAllByReferrerIdOrderByCreatedAtDesc(referrerId).fold(BigDecimal.ZERO) { acc, r -> acc + r.amount }
}
