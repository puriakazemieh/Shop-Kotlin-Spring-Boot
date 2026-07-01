package com.kazemieh.shop.catalog.persistence.entity

import com.kazemieh.shop.identity.persistence.entity.UserEntity
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "recently_viewed",
    uniqueConstraints = [
        UniqueConstraint(name = "uc_recently_viewed_user_product", columnNames = ["user_id", "product_id"])
    ]
)
class RecentlyViewedEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,

    @Column(name = "viewed_at", nullable = false)
    var viewedAt: OffsetDateTime = OffsetDateTime.now()
)
