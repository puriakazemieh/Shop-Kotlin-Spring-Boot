package com.kazemieh.shop.psychtest.api

import com.kazemieh.shop.psychtest.api.dto.*
import com.kazemieh.shop.psychtest.application.AdminPsychTestService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/psych-tests")
@PreAuthorize("hasRole('ADMIN')")
class AdminPsychTestController(
    private val adminPsychTestService: AdminPsychTestService
) {

    @GetMapping
    fun list(): List<PsychTestSummaryResponse> = adminPsychTestService.list()

    @PostMapping
    fun create(@RequestBody req: AdminCreatePsychTestRequest): Map<String, Long> =
        mapOf("id" to adminPsychTestService.create(req))

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long): AdminPsychTestDetailResponse = adminPsychTestService.detail(id)

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @RequestBody req: AdminUpdatePsychTestRequest) =
        adminPsychTestService.update(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = adminPsychTestService.delete(id)

    // ---- تفسیرِ دستیِ مشاور ----
    @GetMapping("/pending-interpretations")
    fun pending(): List<UserPsychTestResponse> = adminPsychTestService.pendingInterpretations()

    @PostMapping("/user-tests/{userTestId}/interpret")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun interpret(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable userTestId: Long,
        @RequestBody req: AdminInterpretRequest
    ) = adminPsychTestService.interpret(principal.id, userTestId, req)
}
