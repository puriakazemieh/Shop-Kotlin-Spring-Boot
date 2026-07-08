package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.AdminCourseRefundRequestResponse
import com.kazemieh.shop.academy.api.dto.AdminReviewRefundRequest
import com.kazemieh.shop.academy.application.CourseRefundService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** بررسیِ درخواست‌هایِ بازگشتِ وجهِ دوره — فقط ادمین. */
@RestController
@RequestMapping("/api/admin/academy/refund-requests")
@PreAuthorize("hasRole('ADMIN')")
class AdminCourseRefundController(
    private val courseRefundService: CourseRefundService
) {
    @GetMapping
    fun list(): List<AdminCourseRefundRequestResponse> = courseRefundService.adminList()

    @PostMapping("/{id}/review")
    fun review(@PathVariable id: Long, @RequestBody req: AdminReviewRefundRequest): AdminCourseRefundRequestResponse =
        courseRefundService.adminReview(id, req.approve, req.adminNote)
}
