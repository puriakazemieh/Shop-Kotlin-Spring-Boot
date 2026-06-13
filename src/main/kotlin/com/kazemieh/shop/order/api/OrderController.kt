package com.kazemieh.shop.order.api

import com.kazemieh.shop.order.api.dto.AdminUpdateShippingRequest
import com.kazemieh.shop.order.api.dto.CreateOrderRequest
import com.kazemieh.shop.order.application.OrderService
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: UserPrincipal) =
        orderService.listMyOrders(principal.id)

    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = orderService.getMyOrder(principal.id, id)

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: CreateOrderRequest
    ) = orderService.create(principal.id, req)

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) {
        orderService.cancelMyOrder(principal.id, id)
    }

    @PatchMapping("/{id}/shipping")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateShipping(
        @PathVariable id: Long,
        @Valid @RequestBody req: AdminUpdateShippingRequest
    ) {
        orderService.updateShipping(id, req)
    }

    @GetMapping("/{id}/track")
    fun track(@PathVariable id: Long) = orderService.trackOrder(id)
}