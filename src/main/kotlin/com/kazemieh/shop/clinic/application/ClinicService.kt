package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.persistence.AppointmentRepository
import com.kazemieh.shop.clinic.persistence.AvailabilitySlotRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.AppointmentEntity
import com.kazemieh.shop.clinic.persistence.entity.AppointmentStatus
import com.kazemieh.shop.clinic.persistence.entity.AvailabilitySlotEntity
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
    private val appointmentRepository: AppointmentRepository
) {

    @Transactional(readOnly = true)
    fun listTherapists(): List<TherapistSummaryResponse> {
        val now = OffsetDateTime.now()
        return therapistRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().map { t ->
            val slots = slotRepository
                .findAllByTherapistIdAndIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(t.id, now)
            TherapistSummaryResponse(
                id = t.id, name = t.name, slug = t.slug, specialty = t.specialty,
                photoUrl = t.photoUrl, sessionPrice = t.sessionPrice, availableSlotCount = slots.size
            )
        }
    }

    @Transactional(readOnly = true)
    fun getTherapist(slug: String): TherapistDetailResponse {
        val t = therapistRepository.findBySlug(slug)
            ?: throw NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND)
        val now = OffsetDateTime.now()
        val slots = slotRepository
            .findAllByTherapistIdAndIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(t.id, now)
            .map { it.toSlotResponse() }
        return TherapistDetailResponse(
            id = t.id, name = t.name, slug = t.slug, specialty = t.specialty, bio = t.bio,
            photoUrl = t.photoUrl, sessionPrice = t.sessionPrice,
            sessionDurationMinutes = t.sessionDurationMinutes, slots = slots
        )
    }

    @Transactional(readOnly = true)
    fun myAppointments(userId: Long): List<AppointmentResponse> =
        appointmentRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

    /** رزروِ اتمیکِ یک بازه. اگر همزمان رزرو شده باشد، خطای SLOT_ALREADY_BOOKED. */
    @Transactional
    fun book(userId: Long, req: BookAppointmentRequest): AppointmentResponse {
        val slot = slotRepository.findByIdForUpdate(req.slotId)
            ?: throw NotFoundException("Slot not found", ErrorCodes.SLOT_NOT_FOUND)
        if (slot.isBooked) throw ConflictException("Slot already booked", ErrorCodes.SLOT_ALREADY_BOOKED)

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
    }

    // ---------- mappers / helpers ----------

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
        notes = notes
    )

    companion object {
        private val DAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
