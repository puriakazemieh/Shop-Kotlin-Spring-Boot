package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * اشتراکِ «قیمت کم شد خبرم کن»: کاربر یک قیمتِ هدف برای یک واریانت ثبت می‌کند و وقتی
 * قیمتِ مؤثر (discountedPrice ?: price) به آن عدد یا کمتر برسد، مطلع می‌شود.
 * قیدِ یکتای (variant_id, user_id) از ثبتِ تکراری جلوگیری می‌کند.
 */
@Entity
@Table(
    name = "price_alerts",
    uniqueConstraints = [UniqueConstraint(name = "ux_price_alert", columnNames = ["variant_id", "user_id"])]
)
class PriceAlertEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Column(name = "variant_id", nullable = false)
    var variantId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "target_price", nullable = false, precision = 12, scale = 2)
    var targetPrice: BigDecimal,

    @Column(nullable = false)
    var notified: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(name = "notified_at")
    var notifiedAt: OffsetDateTime? = null
)
