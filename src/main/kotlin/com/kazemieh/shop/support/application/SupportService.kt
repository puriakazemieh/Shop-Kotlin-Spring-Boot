package com.kazemieh.shop.support.application

import com.kazemieh.shop.support.api.dto.*
import com.kazemieh.shop.support.persistence.SupportTicketRepository
import com.kazemieh.shop.support.persistence.entity.SenderRole
import com.kazemieh.shop.support.persistence.entity.SupportMessageEntity
import com.kazemieh.shop.support.persistence.entity.SupportTicketEntity
import com.kazemieh.shop.support.persistence.entity.TicketStatus
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class SupportService(
    private val ticketRepository: SupportTicketRepository
) {

    @Transactional
    fun createTicket(userId: Long, request: CreateTicketRequest): SupportTicketDetailResponse {
        val ticket = SupportTicketEntity(
            userId = userId,
            subject = request.subject.trim().ifEmpty { "پشتیبانی" },
            status = TicketStatus.OPEN
        )
        ticket.messages.add(
            SupportMessageEntity(ticket = ticket, senderRole = SenderRole.CUSTOMER, body = request.message.trim())
        )
        return ticketRepository.save(ticket).toDetail()
    }

    @Transactional(readOnly = true)
    fun listTickets(userId: Long): List<SupportTicketResponse> =
        ticketRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).map { it.toSummary() }

    @Transactional(readOnly = true)
    fun getTicket(userId: Long, ticketId: Long): SupportTicketDetailResponse {
        val ticket = findOwned(userId, ticketId)
        return ticket.toDetail()
    }

    @Transactional
    fun postMessage(userId: Long, ticketId: Long, request: PostMessageRequest): SupportTicketDetailResponse {
        val ticket = findOwned(userId, ticketId)
        ticket.messages.add(
            SupportMessageEntity(ticket = ticket, senderRole = SenderRole.CUSTOMER, body = request.body.trim())
        )
        ticket.status = TicketStatus.OPEN
        return ticketRepository.save(ticket).toDetail()
    }

    // ---------- Admin ----------
    @Transactional(readOnly = true)
    fun listAllTickets(pageable: Pageable): Page<SupportTicketResponse> =
        ticketRepository.findAllByOrderByUpdatedAtDesc(pageable).map { it.toSummary() }

    @Transactional(readOnly = true)
    fun getTicketAdmin(ticketId: Long): SupportTicketDetailResponse =
        findOrThrow(ticketId).toDetail()

    @Transactional
    fun replyAsAdmin(ticketId: Long, request: PostMessageRequest): SupportTicketDetailResponse {
        val ticket = findOrThrow(ticketId)
        ticket.messages.add(
            SupportMessageEntity(ticket = ticket, senderRole = SenderRole.ADMIN, body = request.body.trim())
        )
        ticket.status = TicketStatus.ANSWERED
        return ticketRepository.save(ticket).toDetail()
    }

    @Transactional
    fun closeTicket(ticketId: Long) {
        val ticket = findOrThrow(ticketId)
        ticket.status = TicketStatus.CLOSED
        ticketRepository.save(ticket)
    }

    private fun findOrThrow(ticketId: Long): SupportTicketEntity =
        ticketRepository.findById(ticketId)
            .orElseThrow { NotFoundException("Ticket not found", ErrorCodes.TICKET_NOT_FOUND) }

    private fun findOwned(userId: Long, ticketId: Long): SupportTicketEntity {
        val ticket = findOrThrow(ticketId)
        if (ticket.userId != userId) {
            throw ForbiddenException("You can only access your own tickets", ErrorCodes.TICKET_ACCESS_DENIED)
        }
        return ticket
    }

    private fun SupportTicketEntity.toSummary() = SupportTicketResponse(
        id = id,
        subject = subject,
        status = status,
        lastMessage = messages.lastOrNull()?.body,
        createdAt = createdAt ?: OffsetDateTime.now(),
        updatedAt = updatedAt ?: OffsetDateTime.now()
    )

    private fun SupportTicketEntity.toDetail() = SupportTicketDetailResponse(
        id = id,
        subject = subject,
        status = status,
        userId = userId,
        messages = messages.map {
            SupportMessageResponse(
                id = it.id,
                senderRole = it.senderRole,
                body = it.body,
                createdAt = it.createdAt ?: OffsetDateTime.now()
            )
        },
        createdAt = createdAt ?: OffsetDateTime.now(),
        updatedAt = updatedAt ?: OffsetDateTime.now()
    )
}
