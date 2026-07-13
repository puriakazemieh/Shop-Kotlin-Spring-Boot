package com.kazemieh.shop.academy.courserequest

import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** درخواستِ دوره از دیدِ کاربر: مشاهده‌ی لیست، ثبتِ درخواست، لایک/آنلایک، درخواست‌های من. */
@RestController
@RequestMapping("/api/course-requests")
class CourseRequestController(
    private val service: CourseRequestService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal?): List<CourseRequestResponse> =
        service.listPublic(principal?.id)

    @GetMapping("/mine")
    fun mine(@AuthenticationPrincipal principal: UserPrincipal): List<CourseRequestResponse> =
        service.listMine(principal.id)

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody req: CreateCourseRequestRequest
    ): CourseRequestResponse = service.create(principal.id, req)

    @PostMapping("/{id}/like")
    fun toggleLike(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): ToggleLikeResponse = service.toggleLike(principal.id, id)
}

/** مدیریتِ درخواست‌های دوره برای ادمین. */
@RestController
@RequestMapping("/api/admin/course-requests")
@PreAuthorize("hasRole('ADMIN')")
class AdminCourseRequestController(
    private val service: CourseRequestService
) {

    @GetMapping
    fun list(): List<CourseRequestResponse> = service.listAll()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PostMapping("/{id}/fulfill")
    fun fulfill(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "true") fulfilled: Boolean
    ): CourseRequestResponse = service.setFulfilled(id, fulfilled)
}
