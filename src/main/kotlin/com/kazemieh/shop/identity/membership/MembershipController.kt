package com.kazemieh.shop.identity.membership

import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/memberships")
class MembershipController(
    private val membershipService: MembershipService
) {
    @GetMapping("/mine")
    fun mine(@AuthenticationPrincipal principal: UserPrincipal): MembershipStatus =
        membershipService.getMyStatus(principal.id)

    @PostMapping("/subscribe")
    fun subscribe(@AuthenticationPrincipal principal: UserPrincipal): MembershipStatus =
        membershipService.subscribe(principal.id)
}
