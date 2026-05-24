package com.kazemieh.shop.payment.api

import com.kazemieh.shop.order.application.OrderService
import com.kazemieh.shop.payment.api.dto.PaymentRequestDto
import com.kazemieh.shop.payment.api.dto.PaymentResponseDto
import com.kazemieh.shop.payment.application.PaymentService
import com.kazemieh.shop.shared.security.UserPrincipal
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payment")
class PaymentController(
    private val paymentService: PaymentService,
    private val orderService: OrderService
) {

    @PostMapping("/request")
    fun requestPayment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PaymentRequestDto
    ): ResponseEntity<PaymentResponseDto> {
        val orderIdLong = request.orderId.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid Order ID format")

        val order = orderService.getMyOrder(principal.id, orderIdLong)
        val amount = order.totalPrice

        val paymentUrl = paymentService.startPayment(orderIdLong, amount, principal.id)
        
        return if (paymentUrl != null) {
            ResponseEntity.ok(PaymentResponseDto(paymentUrl))
        } else {
             ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/callback")
    fun handleCallback(
        @RequestParam(value = "Authority", required = false) authority: String?,
        @RequestParam(value = "Status", required = false) status: String?,
        @RequestParam("order_id") orderId: String,
        response: HttpServletResponse
    ) {
        if (authority != null && status != null) {
            val isSuccess = paymentService.verifyPayment(authority, status)
            
            if (isSuccess) {
                response.sendRedirect("myapp://payment-result?status=success&orderId=$orderId")
                return
            }
        }
        
        response.sendRedirect("myapp://payment-result?status=failed&orderId=$orderId")
    }
}