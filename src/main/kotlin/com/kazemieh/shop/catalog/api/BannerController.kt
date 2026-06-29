package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.BannerResponse
import com.kazemieh.shop.catalog.application.BannerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/banners")
class BannerController(
    private val bannerService: BannerService
) {

    @GetMapping
    fun banners(): List<BannerResponse> = bannerService.listActive()
}
