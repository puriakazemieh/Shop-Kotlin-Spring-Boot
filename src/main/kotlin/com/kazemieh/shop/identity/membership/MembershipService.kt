package com.kazemieh.shop.identity.membership

import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.wallet.application.WalletService
import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

data class MembershipStatus(
    val isActive: Boolean,
    val tier: MembershipTier?,
    val expiresAt: OffsetDateTime?,
    val discountPercent: BigDecimal,
    val price: BigDecimal
)

@Service
class MembershipService(
    private val membershipRepository: MembershipRepository,
    private val userRepository: UserRepository,
    private val walletService: WalletService
) {
    companion object {
        val PRICE: BigDecimal = BigDecimal("49000")
        val DISCOUNT_PERCENT: BigDecimal = BigDecimal("0.05")
        const val DURATION_DAYS = 30L
    }

    @Transactional(readOnly = true)
    fun getMyStatus(userId: Long): MembershipStatus {
        val membership = membershipRepository.findFirstByUserIdOrderByExpiresAtDesc(userId)
        val active = membership != null && membership.expiresAt.isAfter(OffsetDateTime.now())
        return MembershipStatus(
            isActive = active,
            tier = membership?.tier,
            expiresAt = membership?.expiresAt,
            discountPercent = if (active) DISCOUNT_PERCENT else BigDecimal.ZERO,
            price = PRICE
        )
    }

    /** پرداختِ حقِ‌اشتراک از کیفِ‌پول؛ اگر عضویتِ فعالی وجود دارد، ۳۰ روز به آن اضافه می‌شود (تمدید). */
    @Transactional
    fun subscribe(userId: Long): MembershipStatus {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        walletService.addTransaction(userId, PRICE.negate(), TransactionType.PURCHASE, "عضویتِ ویژه (۳۰ روز)", null)

        val existing = membershipRepository.findFirstByUserIdOrderByExpiresAtDesc(userId)
        val base = if (existing != null && existing.expiresAt.isAfter(OffsetDateTime.now())) existing.expiresAt else OffsetDateTime.now()
        membershipRepository.save(
            MembershipEntity(user = user, expiresAt = base.plusDays(DURATION_DAYS))
        )
        return getMyStatus(userId)
    }

    /** درصدِ تخفیفِ فعالِ عضویتِ کاربر — برایِ اعمال در محاسبه‌ی سفارش. */
    @Transactional(readOnly = true)
    fun getActiveDiscountPercent(userId: Long): BigDecimal {
        val membership = membershipRepository.findFirstByUserIdOrderByExpiresAtDesc(userId) ?: return BigDecimal.ZERO
        return if (membership.expiresAt.isAfter(OffsetDateTime.now())) DISCOUNT_PERCENT else BigDecimal.ZERO
    }
}
