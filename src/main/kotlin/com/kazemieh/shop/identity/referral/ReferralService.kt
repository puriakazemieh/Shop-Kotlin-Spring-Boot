package com.kazemieh.shop.identity.referral

import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.order.persistence.entity.OrderEntity
import com.kazemieh.shop.wallet.application.WalletService
import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

@Service
class ReferralService(
    private val userRepository: UserRepository,
    private val referralRewardRepository: ReferralRewardRepository,
    private val walletService: WalletService
) {
    companion object {
        private val REWARD_PERCENT = BigDecimal("0.05")
        private val REWARD_CAP = BigDecimal("100000")
        private const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    @Transactional
    fun getOrCreateCode(userId: Long): String {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        user.referralCode?.let { return it }

        var candidate: String
        do {
            candidate = (1..6).map { CODE_CHARS.random(Random) }.joinToString("")
        } while (userRepository.findByReferralCode(candidate) != null)

        user.referralCode = candidate
        userRepository.save(user)
        return candidate
    }

    @Transactional(readOnly = true)
    fun getMyInfo(userId: Long): ReferralInfoResponse {
        val code = getOrCreateCode(userId)
        val referredCount = userRepository.countByReferredByUserId(userId)
        val totalEarned = referralRewardRepository.sumAmountByReferrerId(userId) ?: BigDecimal.ZERO
        return ReferralInfoResponse(code = code, referredCount = referredCount, totalEarned = totalEarned)
    }

    /**
     * وقتی سفارشِ یک کاربرِ معرفی‌شده برای اولین‌بار پرداخت تایید می‌شود (انتقال به PROCESSING)،
     * درصدی از مبلغ به کیف‌پولِ معرف اضافه می‌شود. idempotent: هر سفارش حداکثر یک‌بار پاداش می‌دهد.
     */
    @Transactional
    fun rewardReferrerIfEligible(order: OrderEntity) {
        val referee = order.user ?: return
        val referrerId = referee.referredByUserId ?: return
        if (referralRewardRepository.existsByOrderId(order.id)) return

        val amount = order.totalPrice.multiply(REWARD_PERCENT)
            .setScale(0, RoundingMode.DOWN)
            .min(REWARD_CAP)
        if (amount <= BigDecimal.ZERO) return

        walletService.addTransaction(
            referrerId, amount, TransactionType.ADJUSTMENT,
            "پاداشِ معرفیِ کاربرِ جدید", "order:${order.id}"
        )
        referralRewardRepository.save(
            ReferralRewardEntity(referrerId = referrerId, refereeId = referee.id, orderId = order.id, amount = amount)
        )
    }
}
