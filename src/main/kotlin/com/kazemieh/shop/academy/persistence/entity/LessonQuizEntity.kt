package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

/**
 * آزمونِ کوتاهِ یک درسِ خاص (checkpoint) — جداگانه از آزمونِ پایانِ دوره (QuizEntity).
 * هر درس حداکثر یک آزمون دارد. برخلافِ آزمونِ پایانِ دوره، قبولی در این آزمون گواهی صادر نمی‌کند.
 */
@Entity
@Table(name = "lesson_quizzes")
class LessonQuizEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "lesson_id", nullable = false, unique = true)
    var lessonId: Long,

    @Column(nullable = false, length = 200)
    var title: String = "آزمونِ این درس",

    @Column(name = "pass_score", nullable = false)
    var passScore: Int = 60,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions", columnDefinition = "jsonb")
    var questions: MutableList<QuizQuestion> = mutableListOf(),

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

/** یک تلاشِ کاربر برای آزمونِ یک درس. */
@Entity
@Table(
    name = "lesson_quiz_attempts",
    indexes = [Index(name = "idx_lesson_quiz_attempt_user", columnList = "user_id, lesson_id")]
)
class LessonQuizAttemptEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "lesson_id", nullable = false)
    var lessonId: Long,

    @Column(nullable = false)
    var score: Int,

    @Column(nullable = false)
    var passed: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
