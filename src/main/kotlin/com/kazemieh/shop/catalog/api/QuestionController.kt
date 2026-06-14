package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.CreateQuestionRequest
import com.kazemieh.shop.catalog.api.dto.QuestionResponse
import com.kazemieh.shop.catalog.api.dto.UpdateQuestionRequest
import com.kazemieh.shop.catalog.application.QuestionService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/questions")
class QuestionController(
    private val questionService: QuestionService
) {

    @GetMapping("/product/{productId}")
    fun getQuestions(@PathVariable productId: Long): List<QuestionResponse> {
        return questionService.getQuestionsByProduct(productId)
    }

    @PostMapping
    fun createQuestion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateQuestionRequest
    ): QuestionResponse {
        return questionService.createQuestion(principal.id, request)
    }

    @PutMapping("/{questionId}")
    fun updateQuestion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable questionId: Long,
        @RequestBody request: UpdateQuestionRequest
    ): QuestionResponse {
        return questionService.updateQuestion(principal.id, questionId, request)
    }

    @DeleteMapping("/{questionId}")
    fun deleteQuestion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable questionId: Long
    ) {
        questionService.deleteQuestion(principal.id, questionId)
    }
}
