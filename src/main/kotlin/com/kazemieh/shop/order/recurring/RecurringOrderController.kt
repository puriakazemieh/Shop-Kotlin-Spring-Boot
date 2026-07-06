package com.kazemieh.shop.order.recurring

import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

data class CreateRecurringOrderRequest(
    val variantId: Long,
    val qty: Int = 1,
    val addressId: Long? = null,
    val intervalDays: Int = 30
)

@RestController
@RequestMapping("/api/recurring-orders")
class RecurringOrderController(
    private val recurringOrderService: RecurringOrderService
) {

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateRecurringOrderRequest
    ): RecurringOrderView =
        recurringOrderService.create(principal.id, request.variantId, request.qty, request.addressId, request.intervalDays)

    @GetMapping("/mine")
    fun listMine(@AuthenticationPrincipal principal: UserPrincipal): List<RecurringOrderView> =
        recurringOrderService.listMine(principal.id)

    @PostMapping("/{id}/cancel")
    fun cancel(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) {
        recurringOrderService.cancel(principal.id, id)
    }
}
