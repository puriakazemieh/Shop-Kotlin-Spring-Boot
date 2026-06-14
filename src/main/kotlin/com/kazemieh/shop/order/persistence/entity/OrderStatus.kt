package com.kazemieh.shop.order.persistence.entity

import com.fasterxml.jackson.databind.JsonNode
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.OffsetDateTime

enum class OrderStatus { PLACED, PROCESSING, SHIPPING, COMPLETED, CANCELLED }

@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_orders_user", columnList = "user_id"),
        Index(name = "idx_orders_status", columnList = "status"),
    ]
)
class OrderEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PLACED,

    @Column(name = "subtotal_price", nullable = false, precision = 12, scale = 2)
    var subtotalPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "shipping_price", nullable = false, precision = 12, scale = 2)
    var shippingPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "wallet_paid_amount", nullable = false, precision = 12, scale = 2)
    var walletPaidAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "gateway_paid_amount", nullable = false, precision = 12, scale = 2)
    var gatewayPaidAmount: BigDecimal = BigDecimal.ZERO,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address_snapshot", nullable = false, columnDefinition = "jsonb")
    var addressSnapshot: JsonNode? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItemEntity> = mutableListOf(),

    @Column(name = "shipping_carrier", length = 80)
    var shippingCarrier: String? = null,

    @Column(name = "tracking_code", length = 120)
    var trackingCode: String? = null,

    @Column(name = "shipped_at")
    var shippedAt: OffsetDateTime? = null,

    @Column(name = "delivered_at")
    var deliveredAt: OffsetDateTime? = null,
)