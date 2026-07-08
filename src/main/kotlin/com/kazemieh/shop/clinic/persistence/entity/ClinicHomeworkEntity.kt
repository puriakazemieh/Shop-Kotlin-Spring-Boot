package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class HomeworkStatus { ASSIGNED, COMPLETED }

/** تکلیف/تمرینِ بینِ‌جلسه‌ای که درمانگر برایِ مراجع تعیین می‌کند. */
@Entity
@Table(
    name = "clinic_homework",
    indexes = [Index(name = "idx_clinic_homework_user", columnList = "user_id, therapist_id")]
)
class ClinicHomeworkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "therapist_id", nullable = false)
    var therapistId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: HomeworkStatus = HomeworkStatus.ASSIGNED,

    @Column(name = "due_date")
    var dueDate: OffsetDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
