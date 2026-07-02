package com.kazemieh.shop.support.api.dto

import com.kazemieh.shop.support.persistence.entity.SenderRole
import com.kazemieh.shop.support.persistence.entity.TicketStatus
import java.time.OffsetDateTime

data class CreateTicketRequest(
    val subject: String,
    val message: String
)

data class PostMessageRequest(
    val body: String
)

data class SupportMessageResponse(
    val id: Long,
    val senderRole: SenderRole,
    val body: String,
    val createdAt: OffsetDateTime
)

data class SupportTicketResponse(
    val id: Long,
    val subject: String,
    val status: TicketStatus,
    val lastMessage: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class SupportTicketDetailResponse(
    val id: Long,
    val subject: String,
    val status: TicketStatus,
    val userId: Long,
    val messages: List<SupportMessageResponse>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
