package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.CertificateResponse
import com.kazemieh.shop.academy.api.dto.CreatePeerCommentRequest
import com.kazemieh.shop.academy.api.dto.PeerCommentResponse
import com.kazemieh.shop.academy.api.dto.CourseDetailResponse
import com.kazemieh.shop.academy.api.dto.CourseSummaryResponse
import com.kazemieh.shop.academy.api.dto.LessonQuizResponse
import com.kazemieh.shop.academy.api.dto.LessonQuizResultResponse
import com.kazemieh.shop.academy.api.dto.MyProjectResponse
import com.kazemieh.shop.academy.api.dto.ProgressResponse
import com.kazemieh.shop.academy.api.dto.ProjectSubmissionResponse
import com.kazemieh.shop.academy.api.dto.QuizResponse
import com.kazemieh.shop.academy.api.dto.QuizResultResponse
import com.kazemieh.shop.academy.api.dto.SubmitLessonQuizRequest
import com.kazemieh.shop.academy.api.dto.SubmitProjectRequest
import com.kazemieh.shop.academy.api.dto.SubmitQuizRequest
import com.kazemieh.shop.academy.api.dto.UpdateProgressRequest
import com.kazemieh.shop.academy.api.dto.WaitlistResponse
import com.kazemieh.shop.academy.application.CourseService
import com.kazemieh.shop.academy.application.LessonQuizService
import com.kazemieh.shop.academy.application.ProjectSubmissionService
import com.kazemieh.shop.academy.application.QuizService
import com.kazemieh.shop.catalog.application.FileStorageService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/** بخشِ نیازمندِ احراز هویت: دوره‌های من، ثبت‌نام، پیشرفت، آزمون و گواهی. */
@RestController
@RequestMapping("/api/academy")
class AcademyController(
    private val courseService: CourseService,
    private val quizService: QuizService,
    private val lessonQuizService: LessonQuizService,
    private val projectSubmissionService: ProjectSubmissionService,
    private val fileStorageService: FileStorageService
) {

    @GetMapping("/my-courses")
    fun myCourses(@AuthenticationPrincipal principal: UserPrincipal): List<CourseSummaryResponse> =
        courseService.myCourses(principal.id)

    @PostMapping("/courses/{courseId}/enroll")
    fun enroll(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): CourseDetailResponse = courseService.enroll(principal.id, courseId)

    /** با بازکردنِ صفحه‌ی دوره صدا زده می‌شود تا نشانِ «به‌روزرسانیِ جدید» پاک شود. */
    @PostMapping("/courses/{courseId}/mark-update-seen")
    fun markUpdateSeen(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ) {
        courseService.clearUpdateFlag(principal.id, courseId)
    }

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

    // ---- آزمونِ کوتاهِ درس (checkpoint، جداگانه از آزمونِ پایانِ دوره) ----
    @GetMapping("/lessons/{lessonId}/quiz")
    fun getLessonQuiz(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable lessonId: Long
    ): LessonQuizResponse = lessonQuizService.getQuiz(lessonId, principal.id)

    @PostMapping("/lessons/{lessonId}/quiz/submit")
    fun submitLessonQuiz(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable lessonId: Long,
        @RequestBody request: SubmitLessonQuizRequest
    ): LessonQuizResultResponse = lessonQuizService.submit(principal.id, lessonId, request)

    // ---- پروژه‌ی پایانی (ارزیابیِ پروژه‌محور) ----
    @PostMapping("/courses/{courseId}/project", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun submitProject(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(value = "note", required = false) note: String?
    ): ProjectSubmissionResponse {
        val url = fileStorageService.saveFile(file)
        return projectSubmissionService.submit(principal.id, courseId, SubmitProjectRequest(fileUrl = url, note = note))
    }

    /** ثبتِ پروژه با لینکِ مستقیم (مثلاً گیت‌هاب/درایو) — بدونِ آپلود، هم‌الگو با ثبتِ videoUrl. */
    @PostMapping("/courses/{courseId}/project/link")
    fun submitProjectLink(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long,
        @RequestBody request: SubmitProjectRequest
    ): ProjectSubmissionResponse = projectSubmissionService.submit(principal.id, courseId, request)

    @GetMapping("/courses/{courseId}/project")
    fun myProject(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): MyProjectResponse {
        val submission = projectSubmissionService.getMine(principal.id, courseId)
        return MyProjectResponse(found = submission != null, submission = submission)
    }

    /** نقدِ همتایان: فهرستِ پروژه‌هایِ تاییدشده‌ی هم‌دوره‌ای‌ها برایِ الهام/یادگیری. */
    @GetMapping("/courses/{courseId}/project/peers")
    fun peerSubmissions(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: Long
    ): List<ProjectSubmissionResponse> = projectSubmissionService.listApprovedForPeerReview(principal.id, courseId)

    @GetMapping("/project/{submissionId}/comments")
    fun peerComments(@PathVariable submissionId: Long): List<PeerCommentResponse> =
        projectSubmissionService.listPeerComments(submissionId)

    @PostMapping("/project/{submissionId}/comments")
    fun addPeerComment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable submissionId: Long,
        @RequestBody request: CreatePeerCommentRequest
    ): PeerCommentResponse = projectSubmissionService.addPeerComment(principal.id, submissionId, request.comment)
}
