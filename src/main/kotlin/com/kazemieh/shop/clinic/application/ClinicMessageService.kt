package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.api.dto.ClinicMessageResponse
import com.kazemieh.shop.clinic.api.dto.MessagingPlanStatusResponse
import com.kazemieh.shop.clinic.persistence.ClinicMessageRepository
import com.kazemieh.shop.clinic.persistence.MessagingPlanRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.ClinicMessageEntity
import com.kazemieh.shop.clinic.persistence.entity.MessageSenderType
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * پیام‌رسانیِ امنِ بینِ‌جلسه‌ای — یک رشته‌گفتگویِ واحد بینِ هر مراجع و درمانگر.
 * مراجعی که پلنِ اشتراکِ نامحدود ندارد، تا سقفِ FREE_MESSAGE_LIMIT پیامِ رایگان می‌فرستد.
 */
@Service
class ClinicMessageService(
    private val messageRepository: ClinicMessageRepository,
    private val messagingPlanRepository: MessagingPlanRepository,
    private val therapistRepository: TherapistRepository
) {

    @Transactional(readOnly = true)
    fun listThread(therapistId: Long, userId: Long): List<ClinicMessageResponse> =
        messageRepository.findAllByTherapistIdAndUserIdOrderByCreatedAtAsc(therapistId, userId).map { it.toResponse() }

    @Transactional
    fun sendAsPatient(userId: Long, therapistId: Long, body: String): ClinicMessageResponse {
        therapistRepository.findById(therapistId).orElseThrow { NotFoundException("Therapist not found", ErrorCodes.THERAPIST_NOT_FOUND) }
        val plan = messagingPlanRepository.findByUserIdAndTherapistId(userId, therapistId)
        if (plan?.active != true) {
            val sentCount = messageRepository.countByTherapistIdAndUserIdAndSenderType(therapistId, userId, MessageSenderType.PATIENT)
            if (sentCount >= FREE_MESSAGE_LIMIT) {
                throw ForbiddenException("Free messaging limit reached", ErrorCodes.MESSAGING_LIMIT_REACHED)
            }
        }
        val saved = messageRepository.save(
            ClinicMessageEntity(therapistId = therapistId, userId = userId, senderType = MessageSenderType.PATIENT, body = body.trim())
        )
        return saved.toResponse()
    }

    @Transactional
    fun sendAsTherapist(therapistId: Long, userId: Long, body: String): ClinicMessageResponse {
        val saved = messageRepository.save(
            ClinicMessageEntity(therapistId = therapistId, userId = userId, senderType = MessageSenderType.THERAPIST, body = body.trim())
        )
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    fun messagingStatus(userId: Long, therapistId: Long): MessagingPlanStatusResponse {
        val plan = messagingPlanRepository.findByUserIdAndTherapistId(userId, therapistId)
        val sentCount = messageRepository.countByTherapistIdAndUserIdAndSenderType(therapistId, userId, MessageSenderType.PATIENT)
        return MessagingPlanStatusResponse(
            therapistId = therapistId,
            active = plan?.active == true,
            freeMessagesRemaining = if (plan?.active == true) Int.MAX_VALUE else (FREE_MESSAGE_LIMIT - sentCount).coerceAtLeast(0).toInt()
        )
    }

    private fun ClinicMessageEntity.toResponse() = ClinicMessageResponse(
        id = id, senderType = senderType.name, body = body, createdAt = createdAt?.toString()
    )

    companion object {
        private const val FREE_MESSAGE_LIMIT = 20L
    }
}
