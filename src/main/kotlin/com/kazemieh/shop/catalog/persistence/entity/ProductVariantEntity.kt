package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "product_variants",
    indexes = [
        Index(name = "idx_variants_product", columnList = "product_id"),
    ]
)
class ProductVariantEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity? = null,

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "product_variant_option_values",
        joinColumns = [JoinColumn(name = "product_variant_id")],
        inverseJoinColumns = [JoinColumn(name = "option_value_id")]
    )
    var optionValues: MutableSet<OptionValueEntity> = mutableSetOf(),

    @Column(nullable = false, unique = true, length = 80)
    var sku: String = "",

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discounted_price", precision = 12, scale = 2)
    var discountedPrice: BigDecimal? = null,

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
) {
    fun addOptionValue(optionValue: OptionValueEntity) {
        optionValues.add(optionValue)
        optionValue.variants.add(this)
    }

    fun removeOptionValue(optionValue: OptionValueEntity) {
        optionValues.remove(optionValue)
        optionValue.variants.remove(this)
    }
}