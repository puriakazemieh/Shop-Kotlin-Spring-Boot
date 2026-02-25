package com.kazemieh.shop.cart.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(
    name = "cart_items",
    indexes = [
        Index(name = "idx_cart_items_cart", columnList = "cart_id"),
        Index(name = "idx_cart_items_variant", columnList = "variant_id")
    ],
    uniqueConstraints = [UniqueConstraint(name = "uq_cart_variant", columnNames = ["cart_id", "variant_id"])]
)
class CartItemEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: CartEntity? = null,

    @Column(name = "variant_id", nullable = false)
    var variantId: Long = 0,

    @Column(name = "qty", nullable = false)
    var qty: Int = 1,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)