package com.kazemieh.shop.order.recurring

import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime

interface RecurringOrderRepository : JpaRepository<RecurringOrderEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<RecurringOrderEntity>
    fun findAllByIsActiveTrueAndNextRunAtBefore(now: OffsetDateTime): List<RecurringOrderEntity>
}
