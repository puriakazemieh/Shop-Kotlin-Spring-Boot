package com.kazemieh.shop.payment.application

import com.kazemieh.shop.order.application.OrderService
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.payment.persistence.PaymentRepository
import com.kazemieh.shop.payment.persistence.entity.PaymentEntity
import com.kazemieh.shop.payment.persistence.entity.PaymentStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class PaymentService(
    private val zarinPalService: ZarinPalService,
    private val paymentRepository: PaymentRepository,
    private val orderService: OrderService
) {

    @Transactional
    fun startPayment(orderId: Long, amount: BigDecimal, userId: Long): String? {
        val payment = PaymentEntity(
            orderId = orderId,
            amount = amount,
            status = PaymentStatus.PENDING,
            authority = ""
        )
        val savedPayment = paymentRepository.save(payment)

        val paymentUrl = zarinPalService.createPaymentRequest(amount.toLong(), orderId.toString())

        if (paymentUrl != null) {
            val authority = paymentUrl.substringAfterLast("/")
            savedPayment.authority = authority
            paymentRepository.save(savedPayment)
        }

        return paymentUrl
    }

    @Transactional
    fun verifyPayment(authority: String, status: String): Boolean {
        val payment = paymentRepository.findByAuthority(authority)
            ?: return false

        if (status != "OK") {
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            orderService.updateStatus(payment.orderId, OrderStatus.CANCELLED)
            return false
        }

        val verificationResponse = zarinPalService.verifyPayment(authority, payment.amount.toLong())

        if (verificationResponse.isSuccess) {
            payment.status = PaymentStatus.SUCCESS
            payment.refId = verificationResponse.refId
            paymentRepository.save(payment)
            orderService.updateStatus(payment.orderId, OrderStatus.PROCESSING)
            return true
        } else {
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            orderService.updateStatus(payment.orderId, OrderStatus.CANCELLED)
            return false
        }
    }
}