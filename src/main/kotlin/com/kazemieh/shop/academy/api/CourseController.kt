package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.CourseDetailResponse
import com.kazemieh.shop.academy.api.dto.CourseSummaryResponse
import com.kazemieh.shop.academy.application.CourseService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** کاتالوگِ عمومیِ دوره‌ها (GETها بدونِ نیازِ حتمیِ لاگین؛ در صورتِ لاگین، وضعیتِ enroll هم برمی‌گردد). */
@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal?): List<CourseSummaryResponse> =
        courseService.listCourses(principal?.id)

    @GetMapping("/{slug}")
    fun detail(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable slug: String
    ): CourseDetailResponse = courseService.getCourseDetail(slug, principal?.id)
}
