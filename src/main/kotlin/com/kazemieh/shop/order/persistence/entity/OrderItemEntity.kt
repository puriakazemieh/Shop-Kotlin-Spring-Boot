package com.kazemieh.shop.order.persistence.entity

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal

@Entity
@Table(
    name = "order_items",
    indexes = [Index(name = "idx_order_items_order", columnList = "order_id")]
)
class OrderItemEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity? = null,

    @Column(name = "variant_id", nullable = false)
    var variantId: Long = 0,

    @Column(name = "qty", nullable = false)
    var qty: Int = 1,

    @Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 2)
    var unitPriceSnapshot: BigDecimal = BigDecimal.ZERO,

    @Column(name = "title_snapshot", nullable = false, length = 255)
    var titleSnapshot: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_snapshot", columnDefinition = "jsonb")
    var optionsSnapshot: JsonNode? = null,
)
