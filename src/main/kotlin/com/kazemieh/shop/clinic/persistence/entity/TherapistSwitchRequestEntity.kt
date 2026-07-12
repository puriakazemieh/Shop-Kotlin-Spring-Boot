package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class SwitchRequestStatus { PENDING, APPROVED, REJECTED }

/** درخواستِ تعویضِ درمانگر — مراجع می‌خواهد به درمانگرِ دیگری منتقل شود. */
@Entity
@Table(name = "therapist_switch_requests")
class TherapistSwitchRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "from_therapist_id", nullable = false)
    var fromTherapistId: Long,

    @Column(name = "to_therapist_id")
    var toTherapistId: Long? = null,

    @Column(columnDefinition = "text")
    var reason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SwitchRequestStatus = SwitchRequestStatus.PENDING,

    @Column(name = "admin_note", columnDefinition = "text")
    var adminNote: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @Column(name = "resolved_at")
    var resolvedAt: OffsetDateTime? = null
)
