package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.CampaignResponse
import com.kazemieh.shop.catalog.application.CampaignService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/campaigns")
class CampaignController(
    private val campaignService: CampaignService
) {

    /** کمپینِ فعالِ جاری (یا null اگر کمپینی فعال نباشد). */
    @GetMapping("/active")
    fun active(
        @AuthenticationPrincipal principal: UserPrincipal?,
    ): CampaignResponse? = campaignService.getActiveCampaign(principal?.id)
}
