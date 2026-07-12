package com.kazemieh.shop.order.api

import com.kazemieh.shop.order.api.dto.AdminReturnRequestResponse
import com.kazemieh.shop.order.api.dto.AdminUpdateReturnRequestRequest
import com.kazemieh.shop.order.application.ReturnRequestService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/return-requests")
class AdminReturnRequestController(
    private val returnRequestService: ReturnRequestService
) {

    @GetMapping
    fun list(): List<AdminReturnRequestResponse> = returnRequestService.adminList()

    @PatchMapping("/{id}")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: AdminUpdateReturnRequestRequest
    ): AdminReturnRequestResponse = returnRequestService.adminUpdateStatus(id, request)
}
