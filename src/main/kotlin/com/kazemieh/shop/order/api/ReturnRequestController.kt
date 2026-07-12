package com.kazemieh.shop.order.api

import com.kazemieh.shop.order.api.dto.CreateReturnRequestRequest
import com.kazemieh.shop.order.api.dto.ReturnRequestResponse
import com.kazemieh.shop.order.application.ReturnRequestService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** درخواستِ مرجوعی/تعویضِ کاربر برای آیتم‌هایِ سفارشِ تحویل‌شده. */
@RestController
@RequestMapping("/api/return-requests")
class ReturnRequestController(
    private val returnRequestService: ReturnRequestService
) {

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateReturnRequestRequest
    ): ReturnRequestResponse = returnRequestService.create(principal.id, request)

    @GetMapping("/mine")
    fun listMine(@AuthenticationPrincipal principal: UserPrincipal): List<ReturnRequestResponse> =
        returnRequestService.listMine(principal.id)
}
