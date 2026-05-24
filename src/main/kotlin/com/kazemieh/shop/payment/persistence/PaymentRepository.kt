package com.kazemieh.shop.payment.persistence

import com.kazemieh.shop.payment.persistence.entity.PaymentEntity
import com.kazemieh.shop.payment.persistence.entity.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByAuthority(authority: String): PaymentEntity?
    fun findAllByOrderId(orderId: Long): List<PaymentEntity>

    // متد جدید برای پیدا کردن پرداخت‌های در انتظار بررسی
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = :status AND p.createdAt < :expirationTime")
    fun findUnverifiedPayments(
        @Param("status") status: PaymentStatus,
        @Param("expirationTime") expirationTime: OffsetDateTime
    ): List<PaymentEntity>
}