package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

enum class RefundRequestStatus { PENDING, APPROVED, REJECTED }

/** درخواستِ بازگشتِ وجهِ یک دوره‌ی دیجیتال، در بازه‌ی گارانتی و پیش از پیشرفتِ زیاد. */
@Entity
@Table(name = "course_refund_requests")
class CourseRefundRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    /** مبلغِ پرداختی‌ای که در صورتِ تأیید، به کیف‌پول بازمی‌گردد (اسنپ‌شات در زمانِ درخواست). */
    @Column(nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(columnDefinition = "text")
    var reason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: RefundRequestStatus = RefundRequestStatus.PENDING,

    @Column(name = "admin_note", columnDefinition = "text")
    var adminNote: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(name = "resolved_at")
    var resolvedAt: OffsetDateTime? = null
)
