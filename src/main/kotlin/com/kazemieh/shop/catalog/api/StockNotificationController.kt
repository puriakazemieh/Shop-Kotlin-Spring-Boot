package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.StockNotificationRequest
import com.kazemieh.shop.catalog.api.dto.StockNotificationResponse
import com.kazemieh.shop.catalog.application.StockNotificationService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * اشتراکِ «موجود شد خبرم کن». نیازمندِ احراز هویت (خارج از مسیرهای عمومیِ /api/products).
 */
@RestController
@RequestMapping("/api/stock-notifications")
class StockNotificationController(
    private val stockNotificationService: StockNotificationService
) {

    @PostMapping
    fun subscribe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: StockNotificationRequest
    ): StockNotificationResponse {
        return stockNotificationService.subscribe(principal.id, request.productId, request.variantId)
    }
}
