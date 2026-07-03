package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

/** نحوه‌ی برگزاریِ جلسه‌ی مشاوره. */
enum class SessionMode { IN_PERSON, ONLINE, PHONE }

/**
 * درمانگر/مشاور — واحدِ ارائه‌ی خدمات مشاوره. هر درمانگر بازه‌های زمانیِ آزاد
 * (AvailabilitySlot) دارد که کاربران می‌توانند برای گرفتنِ نوبت رزرو کنند.
 * برای فروش می‌تواند به یک محصولِ فروشگاه (productId) لینک شود.
 */
@Entity
@Table(name = "therapists")
class TherapistEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(nullable = false, unique = true, length = 180)
    var slug: String,

    @Column(length = 150)
    var specialty: String? = null,

    @Column(columnDefinition = "text")
    var bio: String? = null,

    @Column(name = "photo_url", length = 500)
    var photoUrl: String? = null,

    @Column(name = "session_price", nullable = false, precision = 12, scale = 2)
    var sessionPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "session_duration_minutes", nullable = false)
    var sessionDurationMinutes: Int = 45,

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    var mode: SessionMode = SessionMode.ONLINE,

    /** فقط برای جلسه‌ی حضوری: نشانیِ محلِ برگزاری. */
    @Column(name = "location", length = 300)
    var location: String? = null,

    /** لینکِ اختیاری به محصولِ فروشگاه (برای خرید از طریقِ سبد/سفارش). */
    @Column(name = "product_id")
    var productId: Long? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
