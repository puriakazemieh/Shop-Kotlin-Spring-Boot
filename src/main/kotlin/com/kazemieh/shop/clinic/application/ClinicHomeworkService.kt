package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.AssignHomeworkRequest
import com.kazemieh.shop.clinic.api.dto.HomeworkResponse
import com.kazemieh.shop.clinic.persistence.ClinicHomeworkRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.ClinicHomeworkEntity
import com.kazemieh.shop.clinic.persistence.entity.HomeworkStatus
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/** تکلیف/تمرینِ بینِ‌جلسه‌ای که درمانگر برایِ مراجع تعیین می‌کند. */
@Service
class ClinicHomeworkService(
    private val homeworkRepository: ClinicHomeworkRepository,
    private val therapistRepository: TherapistRepository
) {

    @Transactional
    fun assign(therapistId: Long, userId: Long, req: AssignHomeworkRequest): HomeworkResponse {
        val therapist = therapistRepository.findById(therapistId)
            .orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
        val saved = homeworkRepository.save(
            ClinicHomeworkEntity(
                therapistId = therapistId, userId = userId,
                title = req.title.trim(), description = req.description?.trim()?.ifBlank { null },
                dueDate = req.dueDate
            )
        )
        return saved.toResponse(therapist.name)
    }

    @Transactional(readOnly = true)
    fun listForPatient(therapistId: Long, userId: Long): List<HomeworkResponse> {
        val therapist = therapistRepository.findById(therapistId)
            .orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
        return homeworkRepository.findAllByTherapistIdAndUserIdOrderByCreatedAtDesc(therapistId, userId).map { it.toResponse(therapist.name) }
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<HomeworkResponse> {
        val homework = homeworkRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        val therapistNames = therapistRepository.findAllById(homework.map { it.therapistId }.distinct()).associateBy { it.id }
        return homework.map { it.toResponse(therapistNames[it.therapistId]?.name ?: "درمانگر") }
    }

    @Transactional
    fun complete(userId: Long, homeworkId: Long): HomeworkResponse {
        val homework = homeworkRepository.findByIdAndUserId(homeworkId, userId)
            ?: throw NotFoundException("Homework not found", ErrorCodes.HOMEWORK_NOT_FOUND)
        homework.status = HomeworkStatus.COMPLETED
        homework.completedAt = OffsetDateTime.now()
        val saved = homeworkRepository.save(homework)
        val therapistName = therapistRepository.findById(saved.therapistId).map { it.name }.orElse("درمانگر")
        return saved.toResponse(therapistName)
    }

    private fun ClinicHomeworkEntity.toResponse(therapistName: String) = HomeworkResponse(
        id = id, therapistId = therapistId, therapistName = therapistName,
        title = title, description = description, status = status.name,
        dueDate = dueDate?.toString(), completedAt = completedAt?.toString(), createdAt = createdAt?.toString()
    )
}
