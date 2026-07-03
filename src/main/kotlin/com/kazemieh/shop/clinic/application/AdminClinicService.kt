package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.persistence.AppointmentRepository
import com.kazemieh.shop.clinic.persistence.AvailabilitySlotRepository
import com.kazemieh.shop.clinic.persistence.PatientNoteRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.AppointmentStatus
import com.kazemieh.shop.clinic.persistence.entity.AvailabilitySlotEntity
import com.kazemieh.shop.clinic.persistence.entity.PatientNoteEntity
import com.kazemieh.shop.clinic.persistence.entity.SessionMode
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
    private val appointmentRepository: AppointmentRepository,
    private val patientNoteRepository: PatientNoteRepository
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
            mode = parseMode(req.mode),
            location = req.location,
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
        req.mode?.let { t.mode = parseMode(it) }
        req.location?.let { t.location = it }
        therapistRepository.save(t)
    }

    private fun parseMode(v: String?): SessionMode =
        runCatching { SessionMode.valueOf(v!!.trim().uppercase()) }.getOrDefault(SessionMode.ONLINE)

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

    /**
     * تولیدِ خودکارِ بازه‌ها از یک بازه‌ی کاری، با گامِ slotMinutes (پیش‌فرض = مدتِ جلسه‌ی درمانگر).
     * بازه‌های تداخل‌کننده رد نمی‌شوند؛ ادمین مسئولِ ورودیِ درست است.
     */
    @Transactional
    fun generateSlots(therapistId: Long, req: AdminGenerateSlotsRequest): Int {
        val t = findTherapist(therapistId)
        if (!req.windowEnd.isAfter(req.windowStart)) {
            throw BadRequestException("End must be after start", ErrorCodes.INVALID_INPUT)
        }
        val step = (req.slotMinutes ?: t.sessionDurationMinutes).coerceAtLeast(1).toLong()
        var cursor = req.windowStart
        var created = 0
        while (cursor.plusMinutes(step) <= req.windowEnd) {
            val end = cursor.plusMinutes(step)
            slotRepository.save(AvailabilitySlotEntity(therapist = t, startTime = cursor, endTime = end, isBooked = false))
            cursor = end
            created++
        }
        return created
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

    // ---- یادداشت‌های محرمانه‌ی مراجع (فقط ادمین/مشاور) ----
    @Transactional
    fun addPatientNote(counselorId: Long, appointmentId: Long, req: AdminAddPatientNoteRequest): Long {
        appointmentRepository.findById(appointmentId)
            .orElseThrow { NotFoundException("Appointment not found", ErrorCodes.APPOINTMENT_NOT_FOUND) }
        val note = PatientNoteEntity(appointmentId = appointmentId, counselorId = counselorId, note = req.note.trim())
        return patientNoteRepository.save(note).id
    }

    @Transactional(readOnly = true)
    fun listPatientNotes(appointmentId: Long): List<PatientNoteResponse> =
        patientNoteRepository.findAllByAppointmentIdOrderByCreatedAtDesc(appointmentId).map {
            PatientNoteResponse(
                id = it.id, appointmentId = it.appointmentId, counselorId = it.counselorId,
                note = it.note, createdAt = it.createdAt.toString()
            )
        }

    private fun findTherapist(id: Long): TherapistEntity =
        therapistRepository.findById(id)
            .orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
}
