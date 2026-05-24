package com.kazemieh.shop.payment.application

import com.kazemieh.shop.order.application.OrderService
import com.kazemieh.shop.order.persistence.OrderRepository
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.payment.persistence.PaymentRepository
import com.kazemieh.shop.payment.persistence.entity.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class TransactionCleanupService(
    private val orderRepository: OrderRepository,
    private val orderService: OrderService,
    private val paymentRepository: PaymentRepository,
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(TransactionCleanupService::class.java)

    /**
     * 1. جلوگیری از حبس شدن موجودی کالا (کنسل کردن سفارش‌های پرداخت نشده)
     * این متد هر 5 دقیقه اجرا می‌شود.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    @Transactional
    fun cancelExpiredOrders() {
        logger.info("Starting job: Canceling expired PLACED orders")
        
        // پیدا کردن سفارش‌هایی که 30 دقیقه از ایجادشان گذشته و هنوز PLACED هستند
        val expirationTime = OffsetDateTime.now().minusMinutes(30)
        val expiredOrders = orderRepository.findExpiredOrders(OrderStatus.PLACED, expirationTime)

        var count = 0
        for (order in expiredOrders) {
            try {
                // استفاده از متد updateStatus موجود در OrderService که منطق آزادسازی موجودی (inventory.reserved -= qty) را دارد
                orderService.updateStatus(order.id, OrderStatus.CANCELLED)
                count++
            } catch (e: Exception) {
                logger.error("Failed to cancel expired order ${order.id}", e)
            }
        }
        
        if (count > 0) {
            logger.info("Successfully canceled $count expired orders and released their inventory")
        }
    }

    /**
     * 3. مدیریت قطع شدن اینترنت (بررسی تراکنش‌های معلق با بانک)
     * این متد هر 15 دقیقه اجرا می‌شود.
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    @Transactional
    fun verifyPendingPayments() {
        logger.info("Starting job: Verifying pending payments with Bank")
        
        // پیدا کردن پرداخت‌هایی که بیشتر از 15 دقیقه از ایجادشان گذشته و هنوز PENDING هستند
        // به بانک‌های ایرانی نباید پشت سر هم و بلافاصله درخواست داد، باید به کاربر فرصت پرداخت در درگاه را داد
        val expirationTime = OffsetDateTime.now().minusMinutes(15)
        val unverifiedPayments = paymentRepository.findUnverifiedPayments(PaymentStatus.PENDING, expirationTime)

        var verifiedCount = 0
        var failedCount = 0

        for (payment in unverifiedPayments) {
            try {
                // تلاش برای وریفای با بانک (در اینجا فرض می‌کنیم دکمه انصراف زده نشده و باید چک کنیم آیا پول کسر شده یا نه)
                // از آنجایی که ما Status کاربر را نداریم، "OK" را پاس می‌دهیم تا سرویس سعی کند با بانک حرف بزند
                // این بستگی به نحوه پیاده سازی زرین پال دارد. در صورتیکه تراکنش ناموفق در سمت بانک باشد، خروجی false خواهد بود.
                val result = paymentService.verifyPayment(payment.authority, "OK")
                
                if (result) {
                    logger.info("Automatically verified payment ${payment.id} for order ${payment.orderId}")
                    verifiedCount++
                } else {
                     // اگر بانک خطا داد، پرداخت ناموفق ثبت می‌شود
                    logger.info("Automatically failed pending payment ${payment.id} for order ${payment.orderId}")
                    failedCount++
                }
            } catch (e: Exception) {
                logger.error("Error verifying payment ${payment.id} during background job", e)
            }
        }
        
        if (verifiedCount > 0 || failedCount > 0) {
            logger.info("Background verification job finished. Verified: $verifiedCount, Failed: $failedCount")
        }
    }
}