package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** پرسش‌وپاسخِ مخصوصِ یک درس — مینی‌فروم زیرِ همان ویدیو، جدا از تیکتِ پشتیبانیِ کلی. */
@Entity
@Table(name = "lesson_questions", indexes = [Index(name = "idx_lesson_questions_lesson", columnList = "lesson_id")])
class LessonQuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "lesson_id", nullable = false)
    var lessonId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(columnDefinition = "text", nullable = false)
    var content: String,

    @Column(name = "parent_id")
    var parentId: Long? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
