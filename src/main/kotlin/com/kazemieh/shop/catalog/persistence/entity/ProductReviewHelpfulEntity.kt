package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * رأیِ «مفید بود» یک کاربر روی یک نظر. قیدِ یکتای (review_id, user_id)
 * تضمین می‌کند هر کاربر فقط یک بار می‌تواند یک نظر را «مفید» علامت بزند.
 */
@Entity
@Table(
    name = "product_review_helpful",
    uniqueConstraints = [UniqueConstraint(name = "ux_review_helpful", columnNames = ["review_id", "user_id"])]
)
class ProductReviewHelpfulEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    var review: ProductReviewEntity,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
