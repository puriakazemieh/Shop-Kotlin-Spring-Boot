package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = [UniqueConstraint(name = "uq_variant_unique_combo", columnNames = ["product_id", "size_id", "color_id"])],
    indexes = [
        Index(name = "idx_variants_product", columnList = "product_id"),
        Index(name = "idx_variants_size", columnList = "size_id"),
        Index(name = "idx_variants_color", columnList = "color_id"),
    ]
)
class ProductVariantEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    var size: SizeEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id", nullable = false)
    var color: ColorEntity? = null,

    @Column(nullable = false, unique = true, length = 80)
    var sku: String = "",

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "compare_at_price", precision = 12, scale = 2)
    var compareAtPrice: BigDecimal? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)