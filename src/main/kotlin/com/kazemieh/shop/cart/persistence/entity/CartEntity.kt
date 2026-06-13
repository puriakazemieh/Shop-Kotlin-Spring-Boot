package com.kazemieh.shop.cart.persistence.entity

import com.kazemieh.shop.discount.persistence.entity.DiscountEntity
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(
    name = "carts",
    indexes = [Index(name = "idx_carts_user", columnList = "user_id")],
    uniqueConstraints = [UniqueConstraint(name = "uq_carts_user", columnNames = ["user_id"])]
)
class CartEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    var discount: DiscountEntity? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<CartItemEntity> = mutableListOf()
)
