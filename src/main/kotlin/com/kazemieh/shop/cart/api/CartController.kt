package com.kazemieh.shop.cart.api

import com.kazemieh.shop.cart.api.dto.*
import com.kazemieh.shop.cart.application.CartService
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService
) {

    @GetMapping
    fun get(@AuthenticationPrincipal principal: UserPrincipal) =
        cartService.getCart(principal.id)

    @PostMapping("/items")
    fun addItem(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: AddCartItemRequest
    ) = cartService.addItem(principal.id, req)

    @PatchMapping("/items/{itemId}")
    fun updateQty(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable itemId: Long,
        @Valid @RequestBody req: UpdateCartItemRequest
    ) = cartService.updateItemQty(principal.id, itemId, req)

    @DeleteMapping("/items/{itemId}")
    fun remove(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable itemId: Long
    ) = cartService.removeItem(principal.id, itemId)

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clear(@AuthenticationPrincipal principal: UserPrincipal) {
        cartService.clear(principal.id)
    }

    @PostMapping("/items/{itemId}/save-for-later")
    fun saveForLater(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable itemId: Long
    ) = cartService.saveForLater(principal.id, itemId)

    @PostMapping("/items/{itemId}/move-to-cart")
    fun moveToCart(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable itemId: Long
    ) = cartService.moveToCart(principal.id, itemId)


    @PutMapping("/items/{variantId}")
    fun setVariantQty(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable variantId: Long,
        @Valid @RequestBody req: SetCartVariantQtyRequest
    ) = cartService.setVariantQty(principal.id, variantId, req)

    @PatchMapping("/items/{variantId}/adjust")
    fun adjustVariantQty(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable variantId: Long,
        @Valid @RequestBody req: AdjustCartVariantQtyRequest
    ) = cartService.adjustVariantQty(principal.id, variantId, req)

    @PostMapping("/discount")
    fun applyDiscount(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: ApplyDiscountRequest
    ) = cartService.applyDiscount(principal.id, req)

    @DeleteMapping("/discount")
    fun removeDiscount(@AuthenticationPrincipal principal: UserPrincipal) =
        cartService.removeDiscount(principal.id)
}
