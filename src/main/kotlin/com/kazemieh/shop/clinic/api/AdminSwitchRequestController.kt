package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.AdminReviewSwitchRequest
import com.kazemieh.shop.clinic.api.dto.AdminSwitchRequestResponse
import com.kazemieh.shop.clinic.application.TherapistSwitchService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** بررسیِ درخواست‌های تعویضِ درمانگر — فقط ادمین. */
@RestController
@RequestMapping("/api/admin/clinic/switch-requests")
@PreAuthorize("hasRole('ADMIN')")
class AdminSwitchRequestController(
    private val therapistSwitchService: TherapistSwitchService
) {
    @GetMapping
    fun list(): List<AdminSwitchRequestResponse> = therapistSwitchService.adminList()

    @PostMapping("/{id}/review")
    fun review(@PathVariable id: Long, @RequestBody req: AdminReviewSwitchRequest): AdminSwitchRequestResponse =
        therapistSwitchService.adminReview(id, req.approve, req.adminNote)
}
