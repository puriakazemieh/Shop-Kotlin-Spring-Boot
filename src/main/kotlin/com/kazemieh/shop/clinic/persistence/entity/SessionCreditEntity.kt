package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*

/**
 * اعتبارِ جلسه‌ی خریداری‌شده‌ی یک کاربر برای یک درمانگرِ خاص.
 * با خریدِ محصولِ لینک‌شده (Therapist.productId) شارژ می‌شود و با هر رزرو یک واحد کم می‌شود.
 * درمانگرهایی که productId ندارند (مشاوره‌ی رایگان/آزمایشی) نیازی به اعتبار ندارند.
 */
@Entity
@Table(
    name = "session_credits",
    uniqueConstraints = [UniqueConstraint(name = "ux_credit_user_therapist", columnNames = ["user_id", "therapist_id"])]
)
class SessionCreditEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "therapist_id", nullable = false)
    var therapistId: Long,

    @Column(nullable = false)
    var remaining: Int = 0
)
