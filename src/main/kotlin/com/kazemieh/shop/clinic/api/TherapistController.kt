package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.TherapistDetailResponse
import com.kazemieh.shop.clinic.api.dto.TherapistSummaryResponse
import com.kazemieh.shop.clinic.application.ClinicService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** کاتالوگِ عمومیِ درمانگرها و بازه‌های آزادِ آن‌ها (بدونِ نیاز به لاگین؛ در صورتِ لاگین، اعتبارِ جلسه هم برمی‌گردد). */
@RestController
@RequestMapping("/api/therapists")
class TherapistController(
    private val clinicService: ClinicService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal?): List<TherapistSummaryResponse> =
        clinicService.listTherapists(principal?.id)

    @GetMapping("/{slug}")
    fun detail(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable slug: String
    ): TherapistDetailResponse = clinicService.getTherapist(slug, principal?.id)
}
