package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.application.AdminClinicService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/therapists")
@PreAuthorize("hasRole('ADMIN')")
class AdminClinicController(
    private val adminClinicService: AdminClinicService
) {

    @GetMapping
    fun list(): List<TherapistSummaryResponse> = adminClinicService.listTherapists()

    @PostMapping
    fun create(@RequestBody req: AdminCreateTherapistRequest): Map<String, Long> =
        mapOf("id" to adminClinicService.createTherapist(req))

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @RequestBody req: AdminUpdateTherapistRequest) =
        adminClinicService.updateTherapist(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = adminClinicService.deleteTherapist(id)

    @PostMapping("/{therapistId}/slots")
    fun addSlot(@PathVariable therapistId: Long, @RequestBody req: AdminAddSlotRequest): Map<String, Long> =
        mapOf("id" to adminClinicService.addSlot(therapistId, req))

    // ---- Appointments (admin) ----
    @PostMapping("/appointments/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun confirmAppointment(@PathVariable id: Long, @RequestBody req: AdminConfirmAppointmentRequest) =
        adminClinicService.confirmAppointment(id, req)

    @PostMapping("/appointments/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun completeAppointment(@PathVariable id: Long) = adminClinicService.completeAppointment(id)
}
