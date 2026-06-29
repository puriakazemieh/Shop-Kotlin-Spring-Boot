package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.CampaignAdminResponse
import com.kazemieh.shop.catalog.api.dto.CampaignUpsertRequest
import com.kazemieh.shop.catalog.application.CampaignService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/campaigns")
@PreAuthorize("hasRole('ADMIN')")
class AdminCampaignController(
    private val campaignService: CampaignService
) {

    @GetMapping
    fun list(): List<CampaignAdminResponse> = campaignService.listAdmin()

    @PostMapping
    fun create(@RequestBody request: CampaignUpsertRequest): CampaignAdminResponse =
        campaignService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: CampaignUpsertRequest,
    ): CampaignAdminResponse = campaignService.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = campaignService.delete(id)
}
