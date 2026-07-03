package com.kazemieh.shop.psychtest.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.OffsetDateTime

/** نحوه‌ی تولیدِ نتیجه: آنیِ خودکار (بر اساسِ بازه‌ی امتیاز) یا تفسیرِ دستی توسطِ مشاور. */
enum class TestResultMode { AUTO, COUNSELOR }

/** یک گزینه‌ی سؤالِ تست با امتیازِ متناظر. */
class TestOption(
    var text: String = "",
    var score: Int = 0
)

class TestQuestion(
    var text: String = "",
    var options: MutableList<TestOption> = mutableListOf()
)

/** بازه‌ی امتیاز → تفسیر (فقط برای resultMode = AUTO). */
class ScoreRange(
    var minScore: Int = 0,
    var maxScore: Int = 0,
    var interpretation: String = ""
)

/**
 * تستِ روان‌شناسیِ قابلِ‌خرید. کاربر تست را می‌خرد (محصولِ لینک‌شده)، سپس انجام می‌دهد.
 * سؤالات/بازه‌ها به‌صورتِ JSON نگه‌داری می‌شوند.
 */
@Entity
@Table(name = "psych_tests")
class PsychTestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, unique = true, length = 220)
    var slug: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discounted_price", precision = 12, scale = 2)
    var discountedPrice: BigDecimal? = null,

    /** لینکِ اختیاری به محصولِ فروشگاه (برای خرید از طریقِ سبد/سفارش). */
    @Column(name = "product_id")
    var productId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "result_mode", nullable = false, length = 20)
    var resultMode: TestResultMode = TestResultMode.AUTO,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb")
    var questions: MutableList<TestQuestion> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ranges", columnDefinition = "jsonb")
    var ranges: MutableList<ScoreRange> = mutableListOf(),

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
