package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** ثبت‌نامِ یک کاربر در یک دوره (دسترسی به محتوا). یکتا per (user, course). */
@Entity
@Table(
    name = "course_enrollments",
    uniqueConstraints = [UniqueConstraint(name = "ux_enrollment_user_course", columnNames = ["user_id", "course_id"])]
)
class EnrollmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    var course: CourseEntity,

    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    var enrolledAt: OffsetDateTime? = null
)
