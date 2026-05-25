package com.kazemieh.shop.payment.application

import com.kazemieh.shop.order.application.OrderService
import com.kazemieh.shop.order.persistence.OrderRepository
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.payment.persistence.PaymentRepository
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
    private val paymentService: PaymentService,
    private val zarinPalService: ZarinPalService
) {
    private val logger = LoggerFactory.getLogger(TransactionCleanupService::class.java)


    @Scheduled(fixedRate = 300000)
    @Transactional
    fun cancelExpiredOrders() {
        logger.info("Starting job: Canceling expired PLACED orders")
        
        val expirationTime = OffsetDateTime.now().minusMinutes(30)
        val expiredOrders = orderRepository.findExpiredOrders(OrderStatus.PLACED, expirationTime)

        var count = 0
        for (order in expiredOrders) {
            try {
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


    @Scheduled(fixedRate = 900000)
    @Transactional
    fun verifyPendingPaymentsInZarinpal() {
        logger.info("Starting job: Checking unverified payments from ZarinPal")
        
        val unverifiedAuthorities = zarinPalService.getUnverifiedTransactions()

        if (unverifiedAuthorities.isEmpty()) {
            logger.info("No unverified successful payments found in ZarinPal.")
            return
        }

        logger.info("Found ${unverifiedAuthorities.size} unverified payments in ZarinPal. Attempting to verify...")

        var verifiedCount = 0

        for (authority in unverifiedAuthorities) {
            try {
                val result = paymentService.verifyPayment(authority, "OK")
                
                if (result) {
                    verifiedCount++
                    logger.info("Successfully recovered and verified payment with authority: $authority")
                } else {
                    logger.error("Failed to recover payment with authority $authority despite ZarinPal reporting it as unverified.")
                }
            } catch (e: Exception) {
                logger.error("Error during background verification for authority $authority", e)
            }
        }
        
        logger.info("Background verification job finished. Successfully recovered $verifiedCount out of ${unverifiedAuthorities.size} payments.")
    }
}