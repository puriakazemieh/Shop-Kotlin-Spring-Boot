package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class AppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED }

/**
 * نوبتِ مشاوره — رزروِ یک بازه‌ی زمانی توسط یک کاربر برای یک درمانگر.
 * پس از تأییدِ ادمین (یا پرداختِ موفق) لینکِ اتاقِ تماسِ تصویری (videoRoomUrl) ست می‌شود.
 */
@Entity
@Table(
    name = "appointments",
    indexes = [Index(name = "idx_appointments_user", columnList = "user_id")]
)
class AppointmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    var therapist: TherapistEntity,

    // چند‌به‌یک (نه یک‌به‌یک): یک بازه می‌تواند در طولِ زمان چند نوبتِ تاریخی داشته باشد
    // (مثلاً یک نوبتِ لغوشده + یک نوبتِ فعال). محافظِ رزروِ همزمان، پرچمِ isBooked با قفلِ ردیف است.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    var slot: AvailabilitySlotEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    @Column(name = "video_room_url", length = 500)
    var videoRoomUrl: String? = null,

    @Column(columnDefinition = "text")
    var notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
