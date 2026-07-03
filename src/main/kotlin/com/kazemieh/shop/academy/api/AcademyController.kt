package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.CertificateResponse
import com.kazemieh.shop.academy.api.dto.CourseDetailResponse
import com.kazemieh.shop.academy.api.dto.CourseSummaryResponse
import com.kazemieh.shop.academy.api.dto.ProgressResponse
import com.kazemieh.shop.academy.api.dto.QuizResponse
import com.kazemieh.shop.academy.api.dto.QuizResultResponse
import com.kazemieh.shop.academy.api.dto.SubmitQuizRequest
import com.kazemieh.shop.academy.api.dto.UpdateProgressRequest
import com.kazemieh.shop.academy.api.dto.WaitlistResponse
import com.kazemieh.shop.academy.application.CourseService
import com.kazemieh.shop.academy.application.QuizService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** بخشِ نیازمندِ احراز هویت: دوره‌های من، ثبت‌نام، پیشرفت، آزمون و گواهی. */
@RestController
@RequestMapping("/api/academy")
class AcademyController(
    private val courseService: CourseService,
    private val quizService: QuizService
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

    // ---- آزمونِ پایانِ دوره ----
    @GetMapping("/courses/{courseId}/quiz")
    fun getQuiz(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): QuizResponse = quizService.getQuiz(courseId, principal.id)

    @PostMapping("/courses/{courseId}/quiz/submit")
    fun submitQuiz(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long,
        @RequestBody request: SubmitQuizRequest
    ): QuizResultResponse = quizService.submit(principal.id, courseId, request)

    // ---- لیستِ انتظارِ کلاسِ حضوریِ پرشده ----
    @PostMapping("/courses/{courseId}/waitlist")
    fun joinWaitlist(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): WaitlistResponse = courseService.joinWaitlist(principal.id, courseId)

    // ---- گواهی‌ها ----
    @GetMapping("/certificates")
    fun myCertificates(@AuthenticationPrincipal principal: UserPrincipal): List<CertificateResponse> =
        quizService.myCertificates(principal.id)
}
