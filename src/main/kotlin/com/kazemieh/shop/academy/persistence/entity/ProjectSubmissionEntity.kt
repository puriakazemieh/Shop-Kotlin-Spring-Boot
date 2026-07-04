package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class ProjectSubmissionStatus { PENDING, APPROVED, REJECTED }

/**
 * پروژه‌ی پایانیِ دوره — ارزیابیِ پروژه‌محور به‌جای/کنارِ آزمونِ چندگزینه‌ای.
 * هر کاربر برای هر دوره حداکثر یک ثبتِ فعال دارد؛ ثبتِ دوباره وضعیت را به PENDING برمی‌گرداند.
 */
@Entity
@Table(
    name = "project_submissions",
    indexes = [Index(name = "idx_project_submissions_course", columnList = "course_id")],
    uniqueConstraints = [UniqueConstraint(name = "ux_project_submission_course_user", columnNames = ["course_id", "user_id"])]
)
class ProjectSubmissionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "file_url", nullable = false, length = 500)
    var fileUrl: String,

    @Column(columnDefinition = "text")
    var note: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ProjectSubmissionStatus = ProjectSubmissionStatus.PENDING,

    @Column(name = "mentor_feedback", columnDefinition = "text")
    var mentorFeedback: String? = null,

    @Column(name = "submitted_at", nullable = false)
    var submittedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "reviewed_at")
    var reviewedAt: OffsetDateTime? = null
)
