package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

/** یک گزینه‌ی سؤالِ آزمون. */
class QuizOption(
    var text: String = "",
    var correct: Boolean = false
)

/** یک سؤالِ آزمون با گزینه‌های چندگانه (فقط یک گزینه‌ی درست). */
class QuizQuestion(
    var text: String = "",
    var options: MutableList<QuizOption> = mutableListOf()
)

/**
 * آزمونِ پایانِ دوره. هر دوره حداکثر یک آزمون دارد. سؤالات به‌صورتِ JSON نگه‌داری می‌شوند.
 * قبولی وقتی است که درصدِ پاسخِ درست ≥ passScore باشد؛ قبولی → صدورِ گواهی.
 */
@Entity
@Table(name = "course_quizzes")
class QuizEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "course_id", nullable = false, unique = true)
    var courseId: Long,

    @Column(nullable = false, length = 200)
    var title: String = "آزمونِ پایانِ دوره",

    /** حداقل درصدِ لازم برای قبولی (۰ تا ۱۰۰). */
    @Column(name = "pass_score", nullable = false)
    var passScore: Int = 60,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb")
    var questions: MutableList<QuizQuestion> = mutableListOf(),

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

/** یک تلاشِ کاربر برای آزمونِ یک دوره. */
@Entity
@Table(
    name = "quiz_attempts",
    indexes = [Index(name = "idx_quiz_attempt_user", columnList = "user_id, course_id")]
)
class QuizAttemptEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    /** درصدِ پاسخِ درست (۰ تا ۱۰۰). */
    @Column(nullable = false)
    var score: Int,

    @Column(nullable = false)
    var passed: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
