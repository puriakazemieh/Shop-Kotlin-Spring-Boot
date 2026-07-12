package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/**
 * بسته‌ی مشاوره‌ی سازمانی — یک صندلیِ جلسه که سازمان (از فازِ W — academy.OrganizationEntity)
 * برایِ یک درمانگرِ مشخص می‌خرد؛ با اختصاص به ایمیلِ یک کارمند، به او اعتبارِ جلسه اعطا می‌شود.
 */
@Entity
@Table(
    name = "clinic_organization_seats",
    indexes = [Index(name = "idx_clinic_org_seats_org", columnList = "organization_id, therapist_id")]
)
class ClinicOrganizationSeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    var organizationId: Long,

    @Column(name = "therapist_id", nullable = false)
    var therapistId: Long,

    @Column(name = "session_count", nullable = false)
    var sessionCount: Int = 1,

    @Column(name = "assigned_user_id")
    var assignedUserId: Long? = null,

    @Column(name = "assigned_email", length = 200)
    var assignedEmail: String? = null,

    @Column(name = "assigned_at")
    var assignedAt: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
