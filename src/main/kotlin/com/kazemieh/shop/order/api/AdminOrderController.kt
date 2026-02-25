package com.kazemieh.shop.order.api

import com.kazemieh.shop.order.api.dto.AdminUpdateOrderStatusRequest
import com.kazemieh.shop.order.api.dto.UpdateOrderStatusRequest
import com.kazemieh.shop.order.application.AdminOrderService
import com.kazemieh.shop.order.application.OrderService
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
class AdminOrderController(
    private val orderService: OrderService,
    private val adminOrderService: AdminOrderService
) {
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateOrderStatusRequest
    ) {
        val newStatus = OrderStatus.valueOf(req.status.trim().uppercase())
        orderService.updateStatus(id, newStatus)
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ) = adminOrderService.list(status, userId, page, size)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long) = adminOrderService.detail(id)

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody req: AdminUpdateOrderStatusRequest
    ) {
        adminOrderService.updateStatus(id, req)
    }
}
