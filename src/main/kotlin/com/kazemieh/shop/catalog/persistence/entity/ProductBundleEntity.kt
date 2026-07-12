package com.kazemieh.shop.catalog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

/**
 * باندل/پکیجِ محصول (مثلِ «باکسِ یادگیری» یا ستِ ساعت+بند). خودِ باندل یک محصولِ واقعیِ
 * قابلِ‌خرید است (productId) با قیمتِ مستقلِ خودش — این یعنی خرید/سبد/تسویه بدونِ هیچ تغییری
 * در موتورِ سفارش کار می‌کند. memberProductIds فقط برای نمایشِ «این باندل شامل چه چیزهایی است».
 */
@Entity
@Table(name = "product_bundles")
class ProductBundleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, unique = true, length = 220)
    var slug: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "member_product_ids", columnDefinition = "jsonb")
    var memberProductIds: MutableList<Long> = mutableListOf(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
