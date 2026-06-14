package com.kazemieh.shop.wallet.persistence.entity

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

enum class WithdrawalStatus {
    PENDING,    // در انتظار بررسی ادمین
    APPROVED,   // تایید شده و در صف پرداخت
    PAID,       // پرداخت شده توسط ادمین
    REJECTED    // رد شده توسط ادمین
}

@Entity
@Table(name = "withdrawal_requests")
class WithdrawalRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "iban", nullable = false, length = 26)
    var iban: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: WithdrawalStatus = WithdrawalStatus.PENDING,

    @Column(name = "admin_note")
    var adminNote: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
