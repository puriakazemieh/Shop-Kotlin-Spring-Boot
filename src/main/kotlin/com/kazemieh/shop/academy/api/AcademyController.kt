package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.CourseDetailResponse
import com.kazemieh.shop.academy.api.dto.CourseSummaryResponse
import com.kazemieh.shop.academy.api.dto.ProgressResponse
import com.kazemieh.shop.academy.api.dto.UpdateProgressRequest
import com.kazemieh.shop.academy.application.CourseService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** بخشِ نیازمندِ احراز هویت: دوره‌های من، ثبت‌نام و پیشرفت. */
@RestController
@RequestMapping("/api/academy")
class AcademyController(
    private val courseService: CourseService
) {

    @GetMapping("/my-courses")
    fun myCourses(@AuthenticationPrincipal principal: UserPrincipal): List<CourseSummaryResponse> =
        courseService.myCourses(principal.id)

    @PostMapping("/courses/{courseId}/enroll")
    fun enroll(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): CourseDetailResponse = courseService.enroll(principal.id, courseId)

    @GetMapping("/courses/{courseId}/progress")
    fun progress(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): ProgressResponse = courseService.getProgress(principal.id, courseId)

    @PostMapping("/lessons/{lessonId}/progress")
    fun updateProgress(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable lessonId: Long,
        @RequestBody request: UpdateProgressRequest
    ): ProgressResponse = courseService.updateLessonProgress(principal.id, lessonId, request)
}
