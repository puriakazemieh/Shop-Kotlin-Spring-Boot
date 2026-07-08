package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

/** نوعِ فعالیتِ آموزشی. */
enum class CourseType { COURSE, SEMINAR, WORKSHOP }

/**
 * شکلِ برگزاری. آنلاین‌ها (ضبط‌شده/زنده) مسیرِ محتوا+پیشرفت دارند؛
 * حضوری/آفلاین مثلِ کالای سفارشی‌اند (مکان/ظرفیت، بدونِ پخشِ آنلاین).
 */
enum class CourseFormat {
    ONLINE_RECORDED, ONLINE_LIVE, IN_PERSON, OFFLINE;

    /** آیا این فرمت محتوایِ آنلاینِ قابلِ‌پخش دارد؟ */
    val isOnline: Boolean get() = this == ONLINE_RECORDED || this == ONLINE_LIVE
}

enum class CourseLevel { BEGINNER, INTERMEDIATE, ADVANCED }

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

    // ---- نوع/فرمتِ برگزاری ----
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 20)
    var courseType: CourseType = CourseType.COURSE,

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    var format: CourseFormat = CourseFormat.ONLINE_RECORDED,

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20)
    var level: CourseLevel? = null,

    /** فقط حضوری/آفلاین: مکانِ برگزاری. */
    @Column(name = "location", length = 300)
    var location: String? = null,

    /** فقط حضوری/آفلاین: ظرفیتِ کلاس (null = نامحدود). */
    @Column(name = "capacity")
    var capacity: Int? = null,

    /** فقط حضوری/آفلاین: تعدادِ صندلیِ رزروشده (برای کنترلِ ظرفیت). */
    @Column(name = "seats_taken", nullable = false)
    var seatsTaken: Int = 0,

    @Column(name = "job_market_badge", nullable = false)
    var jobMarketBadge: Boolean = false,

    @Column(name = "free_update_badge", nullable = false)
    var freeUpdateBadge: Boolean = false,

    @Column(name = "instructor_bio", columnDefinition = "text")
    var instructorBio: String? = null,

    /** مهارت‌های مدرس، جدا با کاما (ساده‌سازی به‌جای جدولِ جدا). */
    @Column(name = "instructor_skills", length = 500)
    var instructorSkills: String? = null,

    /** اگر true باشد، صدورِ گواهی علاوه‌بر قبولیِ آزمون نیازمندِ تأییدِ پروژه‌ی پایانی هم هست. */
    @Column(name = "requires_project_submission", nullable = false)
    var requiresProjectSubmission: Boolean = false,

    /** کدِ تخفیفِ اختصاصیِ مدرس (باید جداگانه در سیستمِ تخفیف هم ساخته شود تا در چک‌اوت قابلِ‌اعمال باشد). */
    @Column(name = "instructor_discount_code", length = 40)
    var instructorDiscountCode: String? = null,

    /** برایِ دوره‌های همگروهی/زنده: تاریخِ شروعِ گروه (نمایشِ «شروعِ دوره: ...»). */
    @Column(name = "cohort_start_date")
    var cohortStartDate: OffsetDateTime? = null,

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
