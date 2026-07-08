package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.persistence.AppointmentRepository
import com.kazemieh.shop.clinic.persistence.AvailabilitySlotRepository
import com.kazemieh.shop.clinic.persistence.SessionCreditRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.AppointmentEntity
import com.kazemieh.shop.clinic.persistence.entity.AppointmentStatus
import com.kazemieh.shop.clinic.persistence.entity.AvailabilitySlotEntity
import com.kazemieh.shop.clinic.persistence.entity.SessionCreditEntity
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class ClinicService(
    private val therapistRepository: TherapistRepository,
    private val slotRepository: AvailabilitySlotRepository,
    private val appointmentRepository: AppointmentRepository,
    private val creditRepository: SessionCreditRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun listTherapists(userId: Long?): List<TherapistSummaryResponse> {
        val now = OffsetDateTime.now()
        return therapistRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().map { t ->
            val slots = slotRepository
                .findAllByTherapistIdAndIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(t.id, now)
            TherapistSummaryResponse(
                id = t.id, name = t.name, slug = t.slug, specialty = t.specialty,
                photoUrl = t.photoUrl, sessionPrice = t.sessionPrice, availableSlotCount = slots.size,
                requiresPurchase = t.productId != null,
                productSlug = t.productId?.let { productRepository.findById(it).orElse(null)?.slug },
                sessionCredits = creditsFor(userId, t.id)
            )
        }
    }

    @Transactional(readOnly = true)
    fun getTherapist(slug: String, userId: Long?): TherapistDetailResponse {
        val t = therapistRepository.findBySlug(slug)
            ?: throw NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND)
        val now = OffsetDateTime.now()
        val slots = slotRepository
            .findAllByTherapistIdAndIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(t.id, now)
            .map { it.toSlotResponse() }
        return TherapistDetailResponse(
            id = t.id, name = t.name, slug = t.slug, specialty = t.specialty, bio = t.bio,
            photoUrl = t.photoUrl, sessionPrice = t.sessionPrice,
            sessionDurationMinutes = t.sessionDurationMinutes, slots = slots,
            requiresPurchase = t.productId != null,
            productSlug = t.productId?.let { productRepository.findById(it).orElse(null)?.slug },
            sessionCredits = creditsFor(userId, t.id),
            mode = t.mode.name,
            location = t.location,
            productId = t.productId
        )
    }

    @Transactional(readOnly = true)
    fun myAppointments(userId: Long): List<AppointmentResponse> =
        appointmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

    /**
     * رزروِ اتمیکِ یک بازه. اگر درمانگر به محصولی لینک شده باشد (requiresPurchase)،
     * کاربر باید اعتبارِ جلسه (از خرید) داشته باشد؛ در غیرِ این‌صورت خطای INSUFFICIENT_SESSION_CREDITS.
     * درمانگرهایی بدونِ لینکِ محصول (مشاوره‌ی رایگان) نیازی به اعتبار ندارند.
     */
    @Transactional
    fun book(userId: Long, req: BookAppointmentRequest): AppointmentResponse {
        val slot = slotRepository.findByIdForUpdate(req.slotId)
            ?: throw NotFoundException("Slot not found", ErrorCodes.SLOT_NOT_FOUND)
        if (slot.isBooked) throw ConflictException("Slot already booked", ErrorCodes.SLOT_ALREADY_BOOKED)

        if (slot.therapist.productId != null) {
            val credit = creditRepository.findByUserIdAndTherapistIdForUpdate(userId, slot.therapist.id)
            if (credit == null || credit.remaining <= 0) {
                throw ForbiddenException("No session credits for this therapist", ErrorCodes.INSUFFICIENT_SESSION_CREDITS)
            }
            credit.remaining -= 1
            creditRepository.save(credit)
        }

        slot.isBooked = true
        slotRepository.save(slot)

        val appointment = AppointmentEntity(
            userId = userId,
            therapist = slot.therapist,
            slot = slot,
            status = AppointmentStatus.PENDING,
            notes = req.notes?.trim()?.ifBlank { null }
        )
        return appointmentRepository.save(appointment).toResponse()
    }

    /** لغوِ نوبت؛ بازه دوباره آزاد و در صورتِ نیاز، اعتبارِ مصرف‌شده بازگردانده می‌شود. */
    @Transactional
    fun cancel(userId: Long, appointmentId: Long) {
        val appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
            ?: throw NotFoundException("Appointment not found", ErrorCodes.APPOINTMENT_NOT_FOUND)
        if (appointment.status == AppointmentStatus.COMPLETED) {
            throw ForbiddenException("Completed appointment cannot be cancelled", ErrorCodes.APPOINTMENT_ACCESS_DENIED)
        }
        appointment.status = AppointmentStatus.CANCELLED
        appointment.slot.isBooked = false
        slotRepository.save(appointment.slot)
        appointmentRepository.save(appointment)

        if (appointment.therapist.productId != null) {
            val credit = creditRepository.findByUserIdAndTherapistIdForUpdate(userId, appointment.therapist.id)
                ?: SessionCreditEntity(userId = userId, therapistId = appointment.therapist.id, remaining = 0)
            credit.remaining += 1
            creditRepository.save(credit)
        }
    }

    /** رسیدِ جلسه، آماده برایِ ارائه به بیمه (مشخصاتِ درمانگر + تاریخ + مبلغ). */
    @Transactional(readOnly = true)
    fun getReceipt(userId: Long, appointmentId: Long): SessionReceiptResponse {
        val appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
            ?: throw NotFoundException("Appointment not found", ErrorCodes.APPOINTMENT_NOT_FOUND)
        val user = userRepository.findById(userId).orElse(null)
        val patientName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "-" }
        return SessionReceiptResponse(
            appointmentId = appointment.id,
            patientName = patientName,
            therapistName = appointment.therapist.name,
            therapistSpecialty = appointment.therapist.specialty,
            sessionMode = appointment.therapist.mode.name,
            sessionDate = "${appointment.slot.startTime.format(DAY_FMT)} ${appointment.slot.startTime.format(TIME_FMT)}",
            sessionDurationMinutes = appointment.therapist.sessionDurationMinutes,
            amountPaid = appointment.therapist.sessionPrice
        )
    }

    // ---------- mappers / helpers ----------

    private fun creditsFor(userId: Long?, therapistId: Long): Int {
        if (userId == null) return 0
        return creditRepository.findByUserIdAndTherapistId(userId, therapistId)?.remaining ?: 0
    }

    private fun AvailabilitySlotEntity.toSlotResponse() = SlotResponse(
        id = id,
        startTime = startTime,
        endTime = endTime,
        dayLabel = startTime.format(DAY_FMT),
        timeLabel = "${startTime.format(TIME_FMT)}–${endTime.format(TIME_FMT)}"
    )

    private fun AppointmentEntity.toResponse() = AppointmentResponse(
        id = id,
        therapistName = therapist.name,
        therapistPhotoUrl = therapist.photoUrl,
        status = status,
        dayLabel = slot.startTime.format(DAY_FMT),
        timeLabel = "${slot.startTime.format(TIME_FMT)}–${slot.endTime.format(TIME_FMT)}",
        videoRoomUrl = videoRoomUrl,
        canJoin = status == AppointmentStatus.CONFIRMED && !videoRoomUrl.isNullOrBlank(),
        notes = notes,
        mode = therapist.mode.name
    )

    companion object {
        private val DAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
