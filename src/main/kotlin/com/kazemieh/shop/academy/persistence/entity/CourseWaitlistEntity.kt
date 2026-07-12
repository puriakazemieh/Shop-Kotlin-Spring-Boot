package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * لیستِ انتظارِ کلاسِ حضوری/آفلاینِ پرشده. کاربر وقتی ظرفیت تکمیل است ثبتِ‌نام می‌شود؛
 * وقتی ادمین صندلیِ آزادشده را به نفرِ اولِ لیست اختصاص می‌دهد، notified=true می‌شود
 * (همان الگویِ StockNotificationEntity، منتها release دستی توسطِ ادمین است چون هنوز
 * مسیرِ لغوِ ثبت‌نام در دامنه وجود ندارد).
 */
@Entity
@Table(
    name = "course_waitlist",
    uniqueConstraints = [UniqueConstraint(name = "ux_course_waitlist", columnNames = ["course_id", "user_id"])]
)
class CourseWaitlistEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(nullable = false)
    var notified: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(name = "notified_at")
    var notifiedAt: OffsetDateTime? = null
)
