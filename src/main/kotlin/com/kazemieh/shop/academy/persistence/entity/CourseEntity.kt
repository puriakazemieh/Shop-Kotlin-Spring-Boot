package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * دوره‌ی آموزشی — واحدِ فروش + محتوا. برای فروش می‌تواند به یک محصول (productId) لینک شود،
 * ولی محتوا (بخش‌ها/درس‌ها) و ثبت‌نام مستقیماً روی خودِ دوره مدیریت می‌شود.
 */
@Entity
@Table(name = "courses")
class CourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, unique = true, length = 220)
    var slug: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(name = "thumbnail_url", length = 500)
    var thumbnailUrl: String? = null,

    @Column(length = 120)
    var instructor: String? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discounted_price", precision = 12, scale = 2)
    var discountedPrice: BigDecimal? = null,

    /** لینکِ اختیاری به محصولِ فروشگاه (برای خرید از طریقِ سبد/سفارش). */
    @Column(name = "product_id")
    var productId: Long? = null,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = true,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    var sections: MutableList<CourseSectionEntity> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
