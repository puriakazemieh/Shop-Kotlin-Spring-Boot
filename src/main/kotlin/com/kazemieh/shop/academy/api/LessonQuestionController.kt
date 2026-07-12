package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.CreateLessonQuestionRequest
import com.kazemieh.shop.academy.api.dto.LessonQuestionResponse
import com.kazemieh.shop.academy.application.LessonQuestionService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/academy/lessons/{lessonId}/questions")
class LessonQuestionController(
    private val lessonQuestionService: LessonQuestionService
) {

    @GetMapping
    fun list(@PathVariable lessonId: Long): List<LessonQuestionResponse> =
        lessonQuestionService.list(lessonId)

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable lessonId: Long,
        @RequestBody request: CreateLessonQuestionRequest
    ): LessonQuestionResponse = lessonQuestionService.create(principal.id, lessonId, request)
}
