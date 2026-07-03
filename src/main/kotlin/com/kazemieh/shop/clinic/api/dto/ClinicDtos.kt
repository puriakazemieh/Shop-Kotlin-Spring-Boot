package com.kazemieh.shop.clinic.api.dto

import com.kazemieh.shop.clinic.persistence.entity.AppointmentStatus
import java.math.BigDecimal
import java.time.OffsetDateTime

// ---------- Public catalog ----------
data class TherapistSummaryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String?,
    val photoUrl: String?,
    val sessionPrice: BigDecimal,
    val availableSlotCount: Int,
    /** آیا رزرو نیازمندِ خریدِ محصولِ لینک‌شده است (therapist.productId != null). */
    val requiresPurchase: Boolean = false,
    /** اسلاگِ محصولِ فروشگاه برای لینک‌کردنِ مستقیم به صفحه‌ی خرید (اگر لینک شده باشد). */
    val productSlug: String? = null,
    /** تعدادِ اعتبارِ جلسه‌ی باقی‌مانده‌ی کاربرِ لاگین‌شده (اگر لاگین نباشد یا نیازی نباشد، 0). */
    val sessionCredits: Int = 0
)

data class SlotResponse(
    val id: Long,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    /** برچسب‌های آماده‌ی نمایش (سمتِ سرور محاسبه می‌شوند تا کلاینت درگیرِ پارس تاریخ نشود). */
    val dayLabel: String,
    val timeLabel: String
)

data class TherapistDetailResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String?,
    val bio: String?,
    val photoUrl: String?,
    val sessionPrice: BigDecimal,
    val sessionDurationMinutes: Int,
    val slots: List<SlotResponse>,
    val requiresPurchase: Boolean = false,
    val productSlug: String? = null,
    val sessionCredits: Int = 0,
    val mode: String = "ONLINE",
    val location: String? = null
)

// ---------- Appointments ----------
data class BookAppointmentRequest(
    val slotId: Long,
    val notes: String? = null
)

data class AppointmentResponse(
    val id: Long,
    val therapistName: String,
    val therapistPhotoUrl: String?,
    val status: AppointmentStatus,
    val dayLabel: String,
    val timeLabel: String,
    val videoRoomUrl: String?,
    /** آیا کاربر می‌تواند واردِ جلسه شود (تأییدشده و لینک آماده). */
    val canJoin: Boolean,
    val notes: String?
)

// ---------- Admin ----------
data class AdminCreateTherapistRequest(
    val name: String,
    val slug: String,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: BigDecimal = BigDecimal.ZERO,
    val sessionDurationMinutes: Int = 45,
    val productId: Long? = null,
    val isActive: Boolean = true,
    val mode: String = "ONLINE",
    val location: String? = null
)

data class AdminUpdateTherapistRequest(
    val name: String? = null,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: BigDecimal? = null,
    val sessionDurationMinutes: Int? = null,
    val isActive: Boolean? = null,
    val mode: String? = null,
    val location: String? = null
)

data class AdminAddSlotRequest(
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime
)

/**
 * تولیدِ خودکارِ بازه‌ها: بازه‌ی کاری [windowStart, windowEnd] به قطعاتِ slotMinutes
 * (پیش‌فرض = مدتِ جلسه‌ی درمانگر) تقسیم می‌شود.
 */
data class AdminGenerateSlotsRequest(
    val windowStart: OffsetDateTime,
    val windowEnd: OffsetDateTime,
    val slotMinutes: Int? = null
)

data class AdminConfirmAppointmentRequest(
    val videoRoomUrl: String
)

// ---------- Patient notes (حساس) ----------
data class AdminAddPatientNoteRequest(
    val note: String
)

data class PatientNoteResponse(
    val id: Long,
    val appointmentId: Long,
    val counselorId: Long,
    val note: String,
    val createdAt: String
)

data class AdminSlotResponse(
    val id: Long,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val isBooked: Boolean
)

data class AdminAppointmentResponse(
    val id: Long,
    val userId: Long,
    val therapistId: Long,
    val therapistName: String,
    val status: AppointmentStatus,
    val dayLabel: String,
    val timeLabel: String,
    val videoRoomUrl: String?,
    val notes: String?
)
