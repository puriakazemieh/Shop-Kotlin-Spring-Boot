package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.SubmitTherapistMatchRequest
import com.kazemieh.shop.clinic.api.dto.TherapistMatchQuestionResponse
import com.kazemieh.shop.clinic.api.dto.TherapistMatchResultResponse
import com.kazemieh.shop.clinic.api.dto.TherapistSummaryResponse
import com.kazemieh.shop.clinic.persistence.TherapistMatchQuestionRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.TherapistMatchQuestionEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * پرسشنامه‌ی تطبیقِ درمانگر — بی‌حالت: کاربر تگ‌هایی را که با او هم‌خوانی دارند انتخاب می‌کند،
 * سرور درمانگرهایی را که تخصص‌شان با بیش‌ترین تگ هم‌پوشانی دارد، پیشنهاد می‌دهد.
 */
@Service
class TherapistMatchService(
    private val questionRepository: TherapistMatchQuestionRepository,
    private val therapistRepository: TherapistRepository
) {

    @Transactional(readOnly = true)
    fun listQuestions(): List<TherapistMatchQuestionResponse> =
        questionRepository.findAllByOrderByDisplayOrderAsc().map {
            TherapistMatchQuestionResponse(id = it.id, questionText = it.questionText, tag = it.tag)
        }

    @Transactional
    fun createQuestion(questionText: String, tag: String, displayOrder: Int): TherapistMatchQuestionResponse {
        val saved = questionRepository.save(
            TherapistMatchQuestionEntity(questionText = questionText.trim(), tag = tag.trim(), displayOrder = displayOrder)
        )
        return TherapistMatchQuestionResponse(id = saved.id, questionText = saved.questionText, tag = saved.tag)
    }

    @Transactional
    fun deleteQuestion(id: Long) {
        questionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun submitMatch(req: SubmitTherapistMatchRequest): List<TherapistMatchResultResponse> {
        val tags = req.selectedTags.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
        if (tags.isEmpty()) return emptyList()
        return therapistRepository.findAllByIsActiveTrueOrderByCreatedAtDesc()
            .map { t ->
                val specialty = t.specialty?.lowercase().orEmpty()
                val score = tags.count { tag -> specialty.contains(tag) }
                t to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { (t, score) ->
                TherapistMatchResultResponse(
                    therapist = TherapistSummaryResponse(
                        id = t.id, name = t.name, slug = t.slug, specialty = t.specialty,
                        photoUrl = t.photoUrl, sessionPrice = t.sessionPrice, availableSlotCount = 0,
                        requiresPurchase = t.productId != null
                    ),
                    matchScore = score
                )
            }
    }
}
