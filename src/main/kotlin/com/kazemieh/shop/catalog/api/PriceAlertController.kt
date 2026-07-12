package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.PriceAlertRequest
import com.kazemieh.shop.catalog.api.dto.PriceAlertResponse
import com.kazemieh.shop.catalog.application.PriceAlertService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** اشتراکِ «قیمت کم شد خبرم کن». نیازمندِ احراز هویت. */
@RestController
@RequestMapping("/api/price-alerts")
class PriceAlertController(
    private val priceAlertService: PriceAlertService
) {

    @PostMapping
    fun subscribe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PriceAlertRequest
    ): PriceAlertResponse =
        priceAlertService.subscribe(principal.id, request.productId, request.variantId, request.targetPrice)

    @GetMapping("/mine")
    fun listMine(@AuthenticationPrincipal principal: UserPrincipal): List<PriceAlertResponse> =
        priceAlertService.listMine(principal.id)
}
