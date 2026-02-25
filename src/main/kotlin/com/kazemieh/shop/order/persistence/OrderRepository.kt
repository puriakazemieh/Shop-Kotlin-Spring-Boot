package com.kazemieh.shop.order.persistence

import com.kazemieh.shop.order.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<OrderEntity>
    fun findByIdAndUserId(id: Long, userId: Long): OrderEntity?
}