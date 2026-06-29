package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

/**
 * کمپین/حراجِ زمان‌دار («پیشنهاد شگفت‌انگیز» صفحه‌ی اصلی).
 * یک کمپینِ فعال با تاریخ پایان، مجموعه‌ای از محصولات را گروه می‌کند.
 */
@Entity
@Table(
    name = "campaigns",
    indexes = [
        Index(name = "idx_campaigns_active", columnList = "is_active"),
        Index(name = "idx_campaigns_ends_at", columnList = "ends_at"),
    ]
)
class CampaignEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 255)
    var title: String = "",

    @Column(name = "ends_at", nullable = false)
    var endsAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "campaign_products",
        joinColumns = [JoinColumn(name = "campaign_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
    var products: MutableList<ProductEntity> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)
