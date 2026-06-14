package com.kazemieh.shop.wallet.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

enum class TransactionType {
    DEPOSIT,    // شارژ مستقیم (درگاه)
    WITHDRAWAL, // برداشت وجه
    PURCHASE,   // خرید محصول
    REFUND,     // بازگشت وجه به کیف پول
    ADJUSTMENT  // اصلاحیه دستی توسط ادمین
}

@Entity
@Table(name = "wallet_transactions")
class WalletTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    var wallet: WalletEntity? = null,

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    var type: TransactionType = TransactionType.DEPOSIT,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "reference_id")
    var referenceId: String? = null, // مثلا ID سفارش یا ID تراکنش درگاه

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
