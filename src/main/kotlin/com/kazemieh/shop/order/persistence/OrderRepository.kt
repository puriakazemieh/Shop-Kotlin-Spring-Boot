package com.kazemieh.shop.order.persistence

import com.kazemieh.shop.order.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<OrderEntity>
    fun findByIdAndUserId(id: Long, userId: Long): OrderEntity?
    override fun findById(id: Long): Optional<OrderEntity>
}