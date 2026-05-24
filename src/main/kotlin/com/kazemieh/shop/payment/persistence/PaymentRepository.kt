package com.kazemieh.shop.payment.persistence

import com.kazemieh.shop.payment.persistence.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByAuthority(authority: String): PaymentEntity?
    fun findAllByOrderId(orderId: Long): List<PaymentEntity>
}