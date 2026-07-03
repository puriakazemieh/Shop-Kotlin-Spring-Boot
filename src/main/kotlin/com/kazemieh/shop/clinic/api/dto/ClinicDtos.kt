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
    val availableSlotCount: Int
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
    val slots: List<SlotResponse>
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
    val isActive: Boolean = true
)

data class AdminUpdateTherapistRequest(
    val name: String? = null,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: BigDecimal? = null,
    val sessionDurationMinutes: Int? = null,
    val isActive: Boolean? = null
)

data class AdminAddSlotRequest(
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime
)

data class AdminConfirmAppointmentRequest(
    val videoRoomUrl: String
)
