package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.CourseRefundRequestRequest
import com.kazemieh.shop.academy.api.dto.CourseRefundRequestResponse
import com.kazemieh.shop.academy.application.CourseRefundService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** گارانتیِ بازگشتِ وجهِ دوره — سمتِ دانشجو. */
@RestController
@RequestMapping("/api/academy")
class CourseRefundController(
    private val courseRefundService: CourseRefundService
) {
    @PostMapping("/courses/{courseId}/refund-request")
    fun requestRefund(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long,
        @RequestBody req: CourseRefundRequestRequest
    ): CourseRefundRequestResponse = courseRefundService.requestRefund(principal.id, courseId, req.reason)

    @GetMapping("/refund-requests/mine")
    fun listMine(@AuthenticationPrincipal principal: UserPrincipal): List<CourseRefundRequestResponse> =
        courseRefundService.listMine(principal.id)
}
