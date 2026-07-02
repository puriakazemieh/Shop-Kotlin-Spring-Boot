package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

/** پیشرفتِ کاربر روی یک درس. یکتا per (user, lesson). */
@Entity
@Table(
    name = "lesson_progress",
    uniqueConstraints = [UniqueConstraint(name = "ux_progress_user_lesson", columnNames = ["user_id", "lesson_id"])]
)
class LessonProgressEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "lesson_id", nullable = false)
    var lessonId: Long,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(nullable = false)
    var completed: Boolean = false,

    @Column(name = "last_position_seconds", nullable = false)
    var lastPositionSeconds: Int = 0,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
