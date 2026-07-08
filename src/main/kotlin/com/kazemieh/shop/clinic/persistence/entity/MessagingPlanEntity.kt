package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * اشتراکِ پیام‌رسانیِ نامحدود برایِ یک مراجع نزدِ یک درمانگرِ مشخص —
 * با خریدِ محصولِ لینک‌شده (Therapist.messagingProductId) فعال می‌شود.
 */
@Entity
@Table(name = "messaging_plans")
class MessagingPlanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "therapist_id", nullable = false)
    var therapistId: Long,

    @Column(nullable = false)
    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
