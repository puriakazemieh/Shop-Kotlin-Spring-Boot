package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_products_category", columnList = "category_id"),
        Index(name = "idx_products_active", columnList = "is_active"),
        Index(name = "idx_products_title", columnList = "title"),
    ]
)
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: CategoryEntity? = null,

    @Column(nullable = false, length = 255)
    var title: String = "",

    @Column(nullable = false, unique = true, length = 280)
    var slug: String = "",

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(name = "base_price", precision = 12, scale = 2)
    var basePrice: BigDecimal? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)