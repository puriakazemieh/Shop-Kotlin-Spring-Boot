package com.kazemieh.shop.identity.referral

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

/** پاداشِ رفرال: وقتی کاربرِ معرفی‌شده اولین سفارشش را پرداخت می‌کند، به معرف اعتبار داده می‌شود. */
@Entity
@Table(
    name = "referral_rewards",
    uniqueConstraints = [UniqueConstraint(name = "ux_referral_reward_order", columnNames = ["order_id"])]
)
class ReferralRewardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "referrer_id", nullable = false)
    var referrerId: Long,

    @Column(name = "referee_id", nullable = false)
    var refereeId: Long,

    @Column(name = "order_id", nullable = false)
    var orderId: Long,

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
