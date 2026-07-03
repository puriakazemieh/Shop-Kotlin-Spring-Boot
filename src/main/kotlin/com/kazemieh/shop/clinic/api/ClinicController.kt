package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.AppointmentResponse
import com.kazemieh.shop.clinic.api.dto.BookAppointmentRequest
import com.kazemieh.shop.clinic.application.ClinicService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** بخشِ نیازمندِ احراز هویت: نوبت‌های من، رزرو و لغو. */
@RestController
@RequestMapping("/api/clinic")
class ClinicController(
    private val clinicService: ClinicService
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
}
