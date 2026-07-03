package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.persistence.AppointmentRepository
import com.kazemieh.shop.clinic.persistence.AvailabilitySlotRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.AppointmentStatus
import com.kazemieh.shop.clinic.persistence.entity.AvailabilitySlotEntity
import com.kazemieh.shop.clinic.persistence.entity.TherapistEntity
import com.kazemieh.shop.shared.error.BadRequestException
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class AdminClinicService(
    private val therapistRepository: TherapistRepository,
    private val slotRepository: AvailabilitySlotRepository,
    private val appointmentRepository: AppointmentRepository
) {

    private val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @Transactional(readOnly = true)
    fun listTherapists(): List<TherapistSummaryResponse> =
        therapistRepository.findAll().map {
            TherapistSummaryResponse(
                id = it.id, name = it.name, slug = it.slug, specialty = it.specialty,
                photoUrl = it.photoUrl, sessionPrice = it.sessionPrice, availableSlotCount = 0
            )
        }

    @Transactional
    fun createTherapist(req: AdminCreateTherapistRequest): Long {
        val slug = req.slug.trim()
        if (therapistRepository.existsBySlug(slug)) {
            throw ConflictException("Therapist slug exists", ErrorCodes.THERAPIST_SLUG_EXISTS)
        }
        val therapist = TherapistEntity(
            name = req.name.trim(),
            slug = slug,
            specialty = req.specialty,
            bio = req.bio,
            photoUrl = req.photoUrl,
            sessionPrice = req.sessionPrice,
            sessionDurationMinutes = req.sessionDurationMinutes,
            productId = req.productId,
            isActive = req.isActive
        )
        return therapistRepository.save(therapist).id
    }

    @Transactional
    fun updateTherapist(id: Long, req: AdminUpdateTherapistRequest) {
        val t = findTherapist(id)
        req.name?.let { t.name = it.trim() }
        req.specialty?.let { t.specialty = it }
        req.bio?.let { t.bio = it }
        req.photoUrl?.let { t.photoUrl = it }
        req.sessionPrice?.let { t.sessionPrice = it }
        req.sessionDurationMinutes?.let { t.sessionDurationMinutes = it }
        req.isActive?.let { t.isActive = it }
        therapistRepository.save(t)
    }

    @Transactional
    fun deleteTherapist(id: Long) {
        therapistRepository.delete(findTherapist(id))
    }

    @Transactional
    fun addSlot(therapistId: Long, req: AdminAddSlotRequest): Long {
        val t = findTherapist(therapistId)
        if (!req.endTime.isAfter(req.startTime)) {
            throw BadRequestException("End time must be after start time", ErrorCodes.INVALID_INPUT)
        }
        val slot = AvailabilitySlotEntity(
            therapist = t,
            startTime = req.startTime,
            endTime = req.endTime,
            isBooked = false
        )
        return slotRepository.save(slot).id
    }

    @Transactional(readOnly = true)
    fun listSlots(therapistId: Long): List<AdminSlotResponse> =
        slotRepository.findAllByTherapistIdOrderByStartTimeAsc(therapistId).map {
            AdminSlotResponse(id = it.id, startTime = it.startTime, endTime = it.endTime, isBooked = it.isBooked)
        }

    @Transactional(readOnly = true)
    fun listAppointments(): List<AdminAppointmentResponse> =
        appointmentRepository.findAllByOrderByCreatedAtDesc().map { a ->
            AdminAppointmentResponse(
                id = a.id,
                userId = a.userId,
                therapistId = a.therapist.id,
                therapistName = a.therapist.name,
                status = a.status,
                dayLabel = a.slot.startTime.format(dayFmt),
                timeLabel = "${a.slot.startTime.format(timeFmt)}–${a.slot.endTime.format(timeFmt)}",
                videoRoomUrl = a.videoRoomUrl,
                notes = a.notes
            )
        }

    /** تأییدِ نوبت توسطِ ادمین + ثبتِ لینکِ اتاقِ تماسِ تصویری. */
    @Transactional
    fun confirmAppointment(appointmentId: Long, req: AdminConfirmAppointmentRequest) {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { NotFoundException("Appointment not found", ErrorCodes.APPOINTMENT_NOT_FOUND) }
        appointment.videoRoomUrl = req.videoRoomUrl.trim()
        appointment.status = AppointmentStatus.CONFIRMED
        appointmentRepository.save(appointment)
    }

    @Transactional
    fun completeAppointment(appointmentId: Long) {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { NotFoundException("Appointment not found", ErrorCodes.APPOINTMENT_NOT_FOUND) }
        appointment.status = AppointmentStatus.COMPLETED
        appointmentRepository.save(appointment)
    }

    private fun findTherapist(id: Long): TherapistEntity =
        therapistRepository.findById(id)
            .orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
}
