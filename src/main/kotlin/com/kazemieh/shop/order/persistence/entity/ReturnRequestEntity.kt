package com.kazemieh.shop.order.persistence.entity

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class ReturnRequestType { RETURN, EXCHANGE }
enum class ReturnRequestStatus { PENDING, APPROVED, REJECTED, COMPLETED }

/** درخواستِ مرجوعی/تعویضِ یک آیتمِ سفارشِ تحویل‌شده. */
@Entity
@Table(
    name = "return_requests",
    indexes = [
        Index(name = "idx_return_requests_user", columnList = "user_id"),
        Index(name = "idx_return_requests_status", columnList = "status")
    ]
)
class ReturnRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    var orderItem: OrderItemEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: ReturnRequestType = ReturnRequestType.RETURN,

    @Column(columnDefinition = "text", nullable = false)
    var reason: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReturnRequestStatus = ReturnRequestStatus.PENDING,

    @Column(name = "admin_note", columnDefinition = "text")
    var adminNote: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(name = "resolved_at")
    var resolvedAt: OffsetDateTime? = null
)
