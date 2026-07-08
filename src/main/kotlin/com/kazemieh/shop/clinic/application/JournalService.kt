package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.JournalEntryRequest
import com.kazemieh.shop.clinic.api.dto.JournalEntryResponse
import com.kazemieh.shop.clinic.persistence.JournalEntryRepository
import com.kazemieh.shop.clinic.persistence.entity.JournalEntryEntity
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** یادداشتِ روزانه‌ی خصوصی؛ به‌صورتِ اختیاری با یک درمانگر به‌اشتراک گذاشته می‌شود. */
@Service
class JournalService(
    private val journalRepository: JournalEntryRepository
) {

    @Transactional
    fun create(userId: Long, req: JournalEntryRequest): JournalEntryResponse {
        val saved = journalRepository.save(
            JournalEntryEntity(userId = userId, content = req.content.trim(), sharedWithTherapistId = req.sharedWithTherapistId)
        )
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<JournalEntryResponse> =
        journalRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

    @Transactional
    fun delete(userId: Long, id: Long) {
        val entry = journalRepository.findByIdAndUserId(id, userId)
            ?: throw NotFoundException("Journal entry not found", ErrorCodes.JOURNAL_ENTRY_NOT_FOUND)
        journalRepository.delete(entry)
    }

    /** یادداشت‌هایی که مراجع صریحاً با این درمانگر به‌اشتراک گذاشته (فقط ادمین/مشاور). */
    @Transactional(readOnly = true)
    fun listSharedWithTherapist(therapistId: Long, userId: Long): List<JournalEntryResponse> =
        journalRepository.findAllBySharedWithTherapistIdAndUserIdOrderByCreatedAtDesc(therapistId, userId).map { it.toResponse() }

    private fun JournalEntryEntity.toResponse() = JournalEntryResponse(
        id = id, content = content, sharedWithTherapistId = sharedWithTherapistId, createdAt = createdAt?.toString()
    )
}
