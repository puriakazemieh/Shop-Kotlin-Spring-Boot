package com.kazemieh.shop.psychtest.api

import com.kazemieh.shop.psychtest.api.dto.*
import com.kazemieh.shop.psychtest.application.PsychTestService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** کاتالوگِ عمومیِ تست‌ها (بدونِ نیازِ حتمیِ لاگین؛ در صورتِ لاگین، وضعیتِ owned هم برمی‌گردد). */
@RestController
@RequestMapping("/api/psych-tests")
class PsychTestController(
    private val psychTestService: PsychTestService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal?): List<PsychTestSummaryResponse> =
        psychTestService.listTests(principal?.id)

    @GetMapping("/{slug}")
    fun detail(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable slug: String
    ): PsychTestDetailResponse = psychTestService.getTest(slug, principal?.id)
}

/** بخشِ نیازمندِ احراز هویت: تست‌های من + انجامِ تست. */
@RestController
@RequestMapping("/api/my-psych-tests")
class MyPsychTestController(
    private val psychTestService: PsychTestService
) {

    @GetMapping
    fun myTests(@AuthenticationPrincipal principal: UserPrincipal): List<UserPsychTestResponse> =
        psychTestService.myTests(principal.id)

    @GetMapping("/{userTestId}/questions")
    fun questions(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable userTestId: Long
    ): PsychTestDetailResponse = psychTestService.getUserTestQuestions(principal.id, userTestId)

    @PostMapping("/{userTestId}/submit")
    fun submit(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable userTestId: Long,
        @RequestBody request: SubmitTestRequest
    ): UserPsychTestResponse = psychTestService.submit(principal.id, userTestId, request)
}
