package com.kazemieh.shop.support.api

import com.kazemieh.shop.support.api.dto.PostMessageRequest
import com.kazemieh.shop.support.api.dto.SupportTicketDetailResponse
import com.kazemieh.shop.support.api.dto.SupportTicketResponse
import com.kazemieh.shop.support.application.SupportService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/support/tickets")
@PreAuthorize("hasRole('ADMIN')")
class AdminSupportController(
    private val supportService: SupportService
) {

    @GetMapping
    fun list(pageable: Pageable): Page<SupportTicketResponse> = supportService.listAllTickets(pageable)

    @GetMapping("/{ticketId}")
    fun detail(@PathVariable ticketId: Long): SupportTicketDetailResponse =
        supportService.getTicketAdmin(ticketId)

    @PostMapping("/{ticketId}/reply")
    fun reply(
        @PathVariable ticketId: Long,
        @RequestBody request: PostMessageRequest
    ): SupportTicketDetailResponse = supportService.replyAsAdmin(ticketId, request)

    @PatchMapping("/{ticketId}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun close(@PathVariable ticketId: Long) = supportService.closeTicket(ticketId)
}
