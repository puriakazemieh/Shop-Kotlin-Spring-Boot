package com.kazemieh.shop.order.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * تاریخچه‌ی زمان‌دارِ تغییرِ وضعیتِ سفارش — برای پرکردنِ تایم‌لاینِ رهگیری با زمان‌های واقعی.
 */
@Entity
@Table(name = "order_status_history")
class OrderStatusHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus,

    @CreationTimestamp
    @Column(name = "at", nullable = false, updatable = false)
    var at: OffsetDateTime? = null
)
