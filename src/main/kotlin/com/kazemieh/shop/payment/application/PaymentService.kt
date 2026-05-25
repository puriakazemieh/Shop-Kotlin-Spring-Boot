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
            ?: return false // پرداخت پیدا نشد

        // 4. جلوگیری از پردازش تکراری (Idempotency)
        if (payment.status != PaymentStatus.PENDING) {
            // این تراکنش قبلاً پردازش شده است. فقط نتیجه قبلی را برمی‌گردانیم.
            return payment.status == PaymentStatus.SUCCESS
        }

        // 2. عدم کنسل کردن سفارش در صورت انصراف کاربر
        if (status != "OK") {
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            // دیگر سفارش را کنسل نمی‌کنیم. کاربر می‌تواند دوباره تلاش کند.
            return false
        }

        val verificationResponse = zarinPalService.verifyPayment(authority, payment.amount.toLong())

        if (verificationResponse.isSuccess) {
            payment.status = PaymentStatus.SUCCESS
            payment.refId = verificationResponse.refId
            paymentRepository.save(payment)

            orderService.updateStatus(payment.orderId, OrderStatus.PROCESSING)

            orderService.clearCartAfterSuccessfulPayment(payment.orderId)

            return true
        } else {
            payment.status = PaymentStatus.FAILED
            paymentRepository.save(payment)
            // در صورت شکست در وریفای هم سفارش را کنسل نمی‌کنیم.
            return false
        }
    }
}