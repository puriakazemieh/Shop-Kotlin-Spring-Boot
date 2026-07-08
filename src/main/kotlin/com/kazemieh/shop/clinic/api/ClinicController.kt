package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.application.ClinicService
import com.kazemieh.shop.clinic.application.MoodCheckInService
import com.kazemieh.shop.clinic.application.TherapistSwitchService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** بخشِ نیازمندِ احراز هویت: نوبت‌های من، رزرو و لغو، ثبتِ خلق‌وخو، درخواستِ تعویضِ درمانگر، رسیدِ جلسه. */
@RestController
@RequestMapping("/api/clinic")
class ClinicController(
    private val clinicService: ClinicService,
    private val moodCheckInService: MoodCheckInService,
    private val therapistSwitchService: TherapistSwitchService
) {

    @GetMapping("/my-appointments")
    fun myAppointments(@AuthenticationPrincipal principal: UserPrincipal): List<AppointmentResponse> =
        clinicService.myAppointments(principal.id)

    @PostMapping("/appointments")
    fun book(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: BookAppointmentRequest
    ): AppointmentResponse = clinicService.book(principal.id, request)

    @PostMapping("/appointments/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = clinicService.cancel(principal.id, id)

    @GetMapping("/appointments/{id}/receipt")
    fun receipt(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): SessionReceiptResponse = clinicService.getReceipt(principal.id, id)

    // ---- ثبتِ روزانه‌ی خلق‌وخو ----
    @PostMapping("/mood-checkins")
    fun submitMood(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: MoodCheckInRequest
    ): MoodCheckInResponse = moodCheckInService.submit(principal.id, request.moodScore, request.note)

    @GetMapping("/mood-checkins")
    fun moodHistory(@AuthenticationPrincipal principal: UserPrincipal): List<MoodCheckInResponse> =
        moodCheckInService.history(principal.id)

    // ---- درخواستِ تعویضِ درمانگر ----
    @PostMapping("/switch-requests")
    fun requestSwitch(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SwitchRequestRequest
    ): SwitchRequestResponse = therapistSwitchService.request(principal.id, request)

    @GetMapping("/switch-requests/mine")
    fun mySwitchRequests(@AuthenticationPrincipal principal: UserPrincipal): List<SwitchRequestResponse> =
        therapistSwitchService.listMine(principal.id)
}
