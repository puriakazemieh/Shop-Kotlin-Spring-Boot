package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

/**
 * بازه‌ی زمانیِ آزادِ یک درمانگر. با رزرو شدن، isBooked=true می‌شود تا دوباره قابلِ رزرو نباشد.
 */
@Entity
@Table(
    name = "availability_slots",
    indexes = [Index(name = "idx_slots_therapist_start", columnList = "therapist_id, start_time")]
)
class AvailabilitySlotEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    var therapist: TherapistEntity,

    @Column(name = "start_time", nullable = false)
    var startTime: OffsetDateTime,

    @Column(name = "end_time", nullable = false)
    var endTime: OffsetDateTime,

    @Column(name = "is_booked", nullable = false)
    var isBooked: Boolean = false,

    /** ظرفیتِ بازه — بیش‌تر از ۱ یعنی جلسه‌ی گروهی. */
    @Column(nullable = false)
    var capacity: Int = 1,

    /** تعدادِ نوبت‌هایِ فعالِ رزروشده روی این بازه. */
    @Column(name = "booked_count", nullable = false)
    var bookedCount: Int = 0
) {
    val isFull: Boolean get() = bookedCount >= capacity
}
