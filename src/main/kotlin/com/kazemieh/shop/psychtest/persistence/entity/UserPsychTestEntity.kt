package com.kazemieh.shop.psychtest.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

/** وضعیتِ یک تستِ خریداری‌شده‌ی کاربر. */
enum class UserTestStatus { PURCHASED, IN_PROGRESS, AWAITING_INTERPRETATION, COMPLETED }

/**
 * یک تستِ خریداری‌شده‌ی کاربر — نمونه‌ای که کاربر انجام می‌دهد و نتیجه می‌گیرد.
 * با خریدِ محصولِ لینک‌شده (PsychTest.productId) یک ردیفِ PURCHASED ساخته می‌شود.
 */
@Entity
@Table(
    name = "user_psych_tests",
    indexes = [Index(name = "idx_user_psych_test_user", columnList = "user_id")]
)
class UserPsychTestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "test_id", nullable = false)
    var testId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: UserTestStatus = UserTestStatus.PURCHASED,

    @Column(name = "total_score")
    var totalScore: Int? = null,

    @Column(columnDefinition = "text")
    var interpretation: String? = null,

    @Column(name = "interpreted_by_counselor_id")
    var interpretedByCounselorId: Long? = null,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
