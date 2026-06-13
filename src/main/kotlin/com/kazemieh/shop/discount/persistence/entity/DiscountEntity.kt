package com.kazemieh.shop.discount.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

enum class DiscountType {
    PERCENTAGE, FIXED_AMOUNT
}

@Entity
@Table(name = "discounts")
class DiscountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var code: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: DiscountType,

    @Column(nullable = false)
    var value: BigDecimal, // If type is PERCENTAGE, this is 0-100. If FIXED_AMOUNT, it's the exact discount

    var maxDiscountAmount: BigDecimal? = null, // Used only if type is PERCENTAGE

    var minOrderAmount: BigDecimal? = null, // Minimum subtotal required

    var startDate: OffsetDateTime? = null,

    var endDate: OffsetDateTime? = null,

    var usageLimit: Int? = null, // Max times this code can be used globally

    var usageCount: Int = 0, // How many times it has been used

    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
