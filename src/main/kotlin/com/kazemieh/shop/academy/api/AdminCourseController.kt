package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.application.AdminCourseService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
class AdminCourseController(
    private val adminCourseService: AdminCourseService
) {

    @GetMapping
    fun list(): List<CourseSummaryResponse> = adminCourseService.list()

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
}
