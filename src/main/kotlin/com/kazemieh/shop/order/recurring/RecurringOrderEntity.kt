package com.kazemieh.shop.order.recurring

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** خریدِ تکراری/subscribe: هر N روز یک‌بار همان واریانت به‌طورِ خودکار سفارش داده می‌شود. */
@Entity
@Table(
    name = "recurring_orders",
    indexes = [
        Index(name = "idx_recurring_orders_next_run", columnList = "is_active, next_run_at"),
        Index(name = "idx_recurring_orders_user", columnList = "user_id")
    ]
)
class RecurringOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Column(name = "variant_id", nullable = false)
    var variantId: Long,

    @Column(nullable = false)
    var qty: Int = 1,

    @Column(name = "address_id")
    var addressId: Long? = null,

    @Column(name = "interval_days", nullable = false)
    var intervalDays: Int = 30,

    @Column(name = "next_run_at", nullable = false)
    var nextRunAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "last_order_id")
    var lastOrderId: Long? = null,

    @Column(name = "last_run_at")
    var lastRunAt: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
