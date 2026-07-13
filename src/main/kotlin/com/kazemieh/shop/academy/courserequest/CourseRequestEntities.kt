package com.kazemieh.shop.academy.courserequest

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * درخواستِ ساختِ دوره از سویِ کاربران. کاربران دوره‌ای که دوست دارند ساخته شود را
 * پیشنهاد می‌دهند و بقیه می‌توانند «لایک» کنند؛ اولویتِ تولید معمولاً با بیشترین لایک است.
 */
@Entity
@Table(name = "course_requests")
class CourseRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    /** شناسه‌ی کاربرِ درخواست‌دهنده (اگر واردِ حساب شده باشد). */
    @Column(name = "requester_user_id")
    var requesterUserId: Long? = null,

    /** نامِ نمایشیِ درخواست‌دهنده (اسنپ‌شات، برای نمایشِ ادمین). */
    @Column(name = "requester_name", length = 120)
    var requesterName: String? = null,

    /** شمارنده‌ی لایک — با جدولِ آرا هم‌گام نگه داشته می‌شود. */
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,

    /** آیا این درخواست توسطِ ادمین «انجام‌شده» علامت خورده است؟ */
    @Column(name = "fulfilled", nullable = false)
    var fulfilled: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)

/**
 * رأیِ (لایکِ) یک کاربر روی یک درخواستِ دوره. یکتاییِ (requestId, userId) از لایکِ تکراری
 * جلوگیری می‌کند و امکانِ toggle می‌دهد.
 */
@Entity
@Table(
    name = "course_request_votes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["request_id", "user_id"])]
)
class CourseRequestVoteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "request_id", nullable = false)
    var requestId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
