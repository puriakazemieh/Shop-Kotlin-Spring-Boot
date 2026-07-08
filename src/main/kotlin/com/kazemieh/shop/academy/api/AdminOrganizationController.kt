package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.application.OrganizationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** مدیریتِ سازمان‌ها و صندلی‌های سازمانی — فقط ادمین. */
@RestController
@RequestMapping("/api/admin/organizations")
@PreAuthorize("hasRole('ADMIN')")
class AdminOrganizationController(
    private val organizationService: OrganizationService
) {
    @GetMapping
    fun list(): List<OrganizationResponse> = organizationService.list()

    @PostMapping
    fun create(@RequestBody req: CreateOrganizationRequest): OrganizationResponse =
        organizationService.create(req.name, req.contactEmail)

    @PostMapping("/{organizationId}/seats")
    fun buySeats(@PathVariable organizationId: Long, @RequestBody req: BuySeatsRequest): List<SeatResponse> =
        organizationService.buySeats(organizationId, req.courseId, req.count)

    @GetMapping("/{organizationId}/seats")
    fun listSeats(@PathVariable organizationId: Long): List<SeatResponse> =
        organizationService.listSeats(organizationId)

    @PostMapping("/{organizationId}/seats/assign")
    fun assignSeat(@PathVariable organizationId: Long, @RequestBody req: AssignSeatRequest): SeatResponse =
        organizationService.assignSeat(organizationId, req.courseId, req.email)
}
