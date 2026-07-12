package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** سازمان/شرکتی که به‌صورتِ گروهی برایِ کارکنانش صندلیِ دوره می‌خرد. */
@Entity
@Table(name = "organizations")
class OrganizationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(name = "contact_email", length = 200)
    var contactEmail: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)

/**
 * یک صندلیِ خریداری‌شده توسطِ یک سازمان برایِ یک دوره — یا هنوز خالی (assignedUserId = null)
 * یا به یک کارمند (با ایمیل) اختصاص داده شده که با اختصاص، ثبت‌نامِ او در دوره هم انجام می‌شود.
 */
@Entity
@Table(name = "organization_seats")
class OrganizationSeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    var organizationId: Long,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

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
