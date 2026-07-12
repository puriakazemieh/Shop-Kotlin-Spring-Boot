package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.MoodCheckInResponse
import com.kazemieh.shop.clinic.persistence.MoodCheckInRepository
import com.kazemieh.shop.clinic.persistence.entity.MoodCheckInEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** ثبتِ روزانه‌ی خلق‌وخو (۱ تا ۵) + تاریخچه برایِ نمودارِ روند در کلاینت. */
@Service
class MoodCheckInService(
    private val repository: MoodCheckInRepository
) {
    @Transactional
    fun submit(userId: Long, moodScore: Int, note: String?): MoodCheckInResponse {
        val saved = repository.save(
            MoodCheckInEntity(userId = userId, moodScore = moodScore.coerceIn(1, 5), note = note?.trim()?.ifBlank { null })
        )
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun history(userId: Long): List<MoodCheckInResponse> =
        repository.findTop30ByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

    private fun MoodCheckInEntity.toResponse() = MoodCheckInResponse(
        id = id, moodScore = moodScore, note = note, createdAt = createdAt?.toString()
    )
}
