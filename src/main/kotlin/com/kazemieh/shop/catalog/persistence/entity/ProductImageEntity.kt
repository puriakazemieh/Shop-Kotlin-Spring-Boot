package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "product_images",
    indexes = [Index(name = "idx_product_images_product", columnList = "product_id")]
)
class ProductImageEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity? = null,

    @Column(nullable = false, columnDefinition = "text")
    var url: String = "",

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,
)