package com.kazemieh.shop.order.api

import com.kazemieh.shop.order.api.dto.UpdateOrderStatusRequest
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
    private val orderService: OrderService
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
}