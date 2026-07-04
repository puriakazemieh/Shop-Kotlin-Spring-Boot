package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.application.AdminCourseService
import com.kazemieh.shop.catalog.application.FileStorageService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
class AdminCourseController(
    private val adminCourseService: AdminCourseService,
    private val fileStorageService: FileStorageService
) {

    @GetMapping
    fun list(): List<CourseSummaryResponse> = adminCourseService.list()

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long): CourseDetailResponse = adminCourseService.getDetail(id)

    @PostMapping
    fun create(@RequestBody req: AdminCreateCourseRequest): Map<String, Long> =
        mapOf("id" to adminCourseService.create(req))

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @RequestBody req: AdminUpdateCourseRequest) =
        adminCourseService.update(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = adminCourseService.delete(id)

    @PostMapping("/{courseId}/sections")
    fun addSection(@PathVariable courseId: Long, @RequestBody req: AdminCreateSectionRequest): Map<String, Long> =
        mapOf("id" to adminCourseService.addSection(courseId, req))

    @PostMapping("/{courseId}/sections/{sectionId}/lessons")
    fun addLesson(
        @PathVariable courseId: Long,
        @PathVariable sectionId: Long,
        @RequestBody req: AdminCreateLessonRequest
    ): Map<String, Long> = mapOf("id" to adminCourseService.addLesson(courseId, sectionId, req))

    // ---- آزمونِ پایانِ دوره (تست‌ساز) ----
    @GetMapping("/{courseId}/quiz")
    fun getQuiz(@PathVariable courseId: Long): QuizResponse? = adminCourseService.getQuiz(courseId)

    @PutMapping("/{courseId}/quiz")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun upsertQuiz(@PathVariable courseId: Long, @RequestBody req: AdminUpsertQuizRequest) =
        adminCourseService.upsertQuiz(courseId, req)

    // ---- فایل‌های ضمیمه‌ی درس (کنارِ ویدیو) ----
    @PostMapping("/{courseId}/lessons/{lessonId}/files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addLessonFile(
        @PathVariable courseId: Long,
        @PathVariable lessonId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("name") name: String
    ): Map<String, Int> {
        val url = fileStorageService.saveFile(file)
        val sizeLabel = "${(file.size / 1024).coerceAtLeast(1)} KB"
        val index = adminCourseService.addLessonFile(courseId, lessonId, AdminAddLessonFileRequest(name = name, url = url, sizeLabel = sizeLabel))
        return mapOf("index" to index)
    }

    /** افزودنِ فایلِ ضمیمه با لینکِ مستقیم (بدونِ آپلود) — هم‌الگو با نحوه‌ی ثبتِ videoUrl. */
    @PostMapping("/{courseId}/lessons/{lessonId}/files/link")
    fun addLessonFileLink(
        @PathVariable courseId: Long,
        @PathVariable lessonId: Long,
        @RequestBody req: AdminAddLessonFileRequest
    ): Map<String, Int> = mapOf("index" to adminCourseService.addLessonFile(courseId, lessonId, req))

    @DeleteMapping("/{courseId}/lessons/{lessonId}/files/{index}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLessonFile(@PathVariable courseId: Long, @PathVariable lessonId: Long, @PathVariable index: Int) =
        adminCourseService.deleteLessonFile(courseId, lessonId, index)

    // ---- آزمونِ کوتاهِ درس (checkpoint) ----
    @GetMapping("/{courseId}/lessons/{lessonId}/quiz")
    fun getLessonQuiz(@PathVariable courseId: Long, @PathVariable lessonId: Long): AdminLessonQuizResponse {
        val quiz = adminCourseService.getLessonQuiz(courseId, lessonId)
        return AdminLessonQuizResponse(found = quiz != null, quiz = quiz)
    }

    @PutMapping("/{courseId}/lessons/{lessonId}/quiz")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun upsertLessonQuiz(@PathVariable courseId: Long, @PathVariable lessonId: Long, @RequestBody req: AdminUpsertLessonQuizRequest) =
        adminCourseService.upsertLessonQuiz(courseId, lessonId, req)

    // ---- پروژه‌های پایانی (ارزیابیِ پروژه‌محور) ----
    @GetMapping("/{courseId}/projects")
    fun listProjects(@PathVariable courseId: Long): List<ProjectSubmissionResponse> =
        adminCourseService.listProjectSubmissions(courseId)

    @PostMapping("/projects/{submissionId}/review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reviewProject(@PathVariable submissionId: Long, @RequestBody req: AdminReviewProjectRequest) =
        adminCourseService.reviewProjectSubmission(submissionId, req)

    // ---- لیستِ انتظارِ کلاسِ حضوری ----
    @GetMapping("/{courseId}/waitlist")
    fun listWaitlist(@PathVariable courseId: Long): List<AdminWaitlistEntryResponse> =
        adminCourseService.listWaitlist(courseId)

    @PostMapping("/{courseId}/waitlist/notify-next")
    fun notifyNext(@PathVariable courseId: Long): AdminNotifyNextResponse =
        adminCourseService.notifyNextInWaitlist(courseId)
}
