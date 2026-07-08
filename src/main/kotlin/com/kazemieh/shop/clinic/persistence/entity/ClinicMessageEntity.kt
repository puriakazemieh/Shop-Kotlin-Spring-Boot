package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class MessageSenderType { PATIENT, THERAPIST }

/** پیامِ امنِ بینِ‌جلسه‌ایِ یک رشته‌گفتگویِ مراجع–درمانگر. */
@Entity
@Table(
    name = "clinic_messages",
    indexes = [Index(name = "idx_clinic_messages_thread", columnList = "therapist_id, user_id, created_at")]
)
class ClinicMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "therapist_id", nullable = false)
    var therapistId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    var senderType: MessageSenderType,

    @Column(columnDefinition = "text", nullable = false)
    var body: String,

    @Column(name = "read_at")
    var readAt: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
