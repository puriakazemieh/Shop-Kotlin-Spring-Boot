package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * اشتراکِ «موجود شد خبرم کن»: کاربر برای یک واریانتِ ناموجود ثبت می‌کند تا هنگامِ
 * شارژِ دوباره‌ی موجودی مطلع شود. قیدِ یکتای (variant_id, user_id) از ثبتِ تکراری جلوگیری می‌کند.
 */
@Entity
@Table(
    name = "stock_notifications",
    uniqueConstraints = [UniqueConstraint(name = "ux_stock_notification", columnNames = ["variant_id", "user_id"])]
)
class StockNotificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Column(name = "variant_id", nullable = false)
    var variantId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    /** وقتی موجودی شارژ شد و کاربر مطلع/علامت‌گذاری شد، true می‌شود. */
    @Column(nullable = false)
    var notified: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(name = "notified_at")
    var notifiedAt: OffsetDateTime? = null
)
