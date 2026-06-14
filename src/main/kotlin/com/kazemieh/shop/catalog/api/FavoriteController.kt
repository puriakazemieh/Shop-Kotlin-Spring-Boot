package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.PageResponse
import com.kazemieh.shop.catalog.api.dto.ProductSummaryResponse
import com.kazemieh.shop.catalog.application.FavoriteService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val favoriteService: FavoriteService
) {

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable productId: Long) {
        favoriteService.addToFavorites(principal.id, productId)
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable productId: Long) {
        favoriteService.removeFromFavorites(principal.id, productId)
    }

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponse<ProductSummaryResponse> =
        favoriteService.getFavorites(principal.id, page, size)
}
