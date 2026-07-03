package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.TherapistDetailResponse
import com.kazemieh.shop.clinic.api.dto.TherapistSummaryResponse
import com.kazemieh.shop.clinic.application.ClinicService
import org.springframework.web.bind.annotation.*

/** کاتالوگِ عمومیِ درمانگرها و بازه‌های آزادِ آن‌ها (بدونِ نیاز به لاگین). */
@RestController
@RequestMapping("/api/therapists")
class TherapistController(
    private val clinicService: ClinicService
) {

    @GetMapping
    fun list(): List<TherapistSummaryResponse> = clinicService.listTherapists()

    @GetMapping("/{slug}")
    fun detail(@PathVariable slug: String): TherapistDetailResponse = clinicService.getTherapist(slug)
}
