package com.kazemieh.shop.catalog.persistence.entity

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "product_reviews")
class ProductReviewEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @Column(nullable = true)
    var rating: Int? = null,

    @Column(columnDefinition = "text", nullable = false)
    var comment: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: ProductReviewEntity? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    var replies: MutableList<ProductReviewEntity> = mutableListOf(),

    @Column(nullable = false)
    var isNew: Boolean = true,

    @Column(name = "helpful_count", nullable = false)
    var helpfulCount: Int = 0,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
