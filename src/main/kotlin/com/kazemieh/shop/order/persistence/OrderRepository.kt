package com.kazemieh.shop.order.persistence

import com.kazemieh.shop.order.persistence.entity.OrderEntity
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.Optional

interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<OrderEntity>
    fun findByIdAndUserId(id: Long, userId: Long): OrderEntity?
    override fun findById(id: Long): Optional<OrderEntity>

    // آمار داشبورد مدیریت
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM OrderEntity o WHERE o.status <> :cancelled")
    fun sumRevenue(@Param("cancelled") cancelled: OrderStatus): BigDecimal

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :from AND o.status <> :cancelled")
    fun findCreatedSince(
        @Param("from") from: OffsetDateTime,
        @Param("cancelled") cancelled: OrderStatus
    ): List<OrderEntity>

    // متد جدید برای پیدا کردن سفارش‌های منقضی شده
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.createdAt < :expirationTime")
    fun findExpiredOrders(
        @Param("status") status: OrderStatus,
        @Param("expirationTime") expirationTime: OffsetDateTime
    ): List<OrderEntity>
}