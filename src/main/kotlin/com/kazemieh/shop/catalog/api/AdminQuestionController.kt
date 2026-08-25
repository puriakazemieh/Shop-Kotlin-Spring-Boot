package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminInteractionResponse
import com.kazemieh.shop.catalog.api.dto.PageResponse
import com.kazemieh.shop.catalog.application.QuestionService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
class AdminQuestionController(
    private val questionService: QuestionService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) productId: Long?,
        @RequestParam(required = false) isNew: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponse<AdminInteractionResponse> {
        return questionService.listQuestionsAdmin(productId, isNew, page, size)
    }
}
