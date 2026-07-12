package com.kazemieh.shop.support.api

import com.kazemieh.shop.support.api.dto.CreateTicketRequest
import com.kazemieh.shop.support.api.dto.PostMessageRequest
import com.kazemieh.shop.support.api.dto.SupportTicketDetailResponse
import com.kazemieh.shop.support.api.dto.SupportTicketResponse
import com.kazemieh.shop.support.application.SupportService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/support/tickets")
class SupportController(
    private val supportService: SupportService
) {

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateTicketRequest
    ): SupportTicketDetailResponse = supportService.createTicket(principal.id, request)

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal): List<SupportTicketResponse> =
        supportService.listTickets(principal.id)

    @GetMapping("/{ticketId}")
    fun detail(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable ticketId: Long
    ): SupportTicketDetailResponse = supportService.getTicket(principal.id, ticketId)

    @PostMapping("/{ticketId}/messages")
    fun postMessage(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable ticketId: Long,
        @RequestBody request: PostMessageRequest
    ): SupportTicketDetailResponse = supportService.postMessage(principal.id, ticketId, request)
}
