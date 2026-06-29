package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

/**
 * بنرِ تبلیغاتیِ صفحه‌ی اصلی. کلیک می‌تواند به یک دسته‌بندی هدایت کند (اختیاری).
 */
@Entity
@Table(
    name = "banners",
    indexes = [
        Index(name = "idx_banners_active", columnList = "is_active"),
        Index(name = "idx_banners_sort", columnList = "sort_order"),
    ]
)
class BannerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 255)
    var title: String = "",

    @Column(length = 255)
    var subtitle: String? = null,

    @Column(name = "image_url", length = 1024)
    var imageUrl: String? = null,

    @Column(name = "category_id")
    var categoryId: Long? = null,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
)
