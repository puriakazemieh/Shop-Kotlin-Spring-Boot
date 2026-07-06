package com.kazemieh.shop.identity.referral

import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/referrals")
class ReferralController(
    private val referralService: ReferralService
) {
    @GetMapping("/mine")
    fun mine(@AuthenticationPrincipal principal: UserPrincipal): ReferralInfoResponse =
        referralService.getMyInfo(principal.id)
}
