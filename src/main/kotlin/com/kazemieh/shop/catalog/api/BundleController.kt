package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.BundleDetailResponse
import com.kazemieh.shop.catalog.api.dto.BundleSummaryResponse
import com.kazemieh.shop.catalog.application.BundleService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** کاتالوگِ عمومیِ باندل‌ها (پکیج‌های ترکیبی). */
@RestController
@RequestMapping("/api/bundles")
class BundleController(
    private val bundleService: BundleService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal?): List<BundleSummaryResponse> =
        bundleService.list(principal?.id)

    @GetMapping("/{slug}")
    fun detail(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable slug: String
    ): BundleDetailResponse = bundleService.detail(slug, principal?.id)
}
