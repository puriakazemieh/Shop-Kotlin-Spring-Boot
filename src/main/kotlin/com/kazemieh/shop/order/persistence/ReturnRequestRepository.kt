package com.kazemieh.shop.order.persistence

import com.kazemieh.shop.order.persistence.entity.ReturnRequestEntity
import com.kazemieh.shop.order.persistence.entity.ReturnRequestStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ReturnRequestRepository : JpaRepository<ReturnRequestEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<ReturnRequestEntity>
    fun findAllByOrderByCreatedAtDesc(): List<ReturnRequestEntity>
    fun findAllByStatusOrderByCreatedAtDesc(status: ReturnRequestStatus): List<ReturnRequestEntity>
    fun existsByOrderItemIdAndStatusNot(orderItemId: Long, status: ReturnRequestStatus): Boolean
}
