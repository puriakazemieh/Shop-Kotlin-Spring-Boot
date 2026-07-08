package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.AssignClinicSeatRequest
import com.kazemieh.shop.clinic.api.dto.BuyClinicSeatsRequest
import com.kazemieh.shop.clinic.api.dto.ClinicSeatResponse
import com.kazemieh.shop.clinic.application.ClinicOrganizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** بسته‌ی مشاوره‌ی سازمانی — صندلیِ جلسه برایِ یک درمانگرِ مشخص؛ سازمان از فازِ W بازاستفاده می‌شود. */
@RestController
@RequestMapping("/api/admin/organizations/{organizationId}/clinic-seats")
@PreAuthorize("hasRole('ADMIN')")
class AdminClinicOrganizationController(
    private val clinicOrganizationService: ClinicOrganizationService
) {
    @PostMapping
    fun buySeats(@PathVariable organizationId: Long, @RequestBody req: BuyClinicSeatsRequest): List<ClinicSeatResponse> =
        clinicOrganizationService.buySeats(organizationId, req.therapistId, req.sessionCount, req.count)

    @GetMapping
    fun listSeats(@PathVariable organizationId: Long): List<ClinicSeatResponse> =
        clinicOrganizationService.listSeats(organizationId)

    @PostMapping("/assign")
    fun assignSeat(@PathVariable organizationId: Long, @RequestBody req: AssignClinicSeatRequest): ClinicSeatResponse =
        clinicOrganizationService.assignSeat(organizationId, req.therapistId, req.email)
}
