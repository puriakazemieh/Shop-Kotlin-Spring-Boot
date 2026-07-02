package com.kazemieh.shop.support.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class SenderRole { CUSTOMER, ADMIN }

@Entity
@Table(name = "support_messages")
class SupportMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: SupportTicketEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false, length = 20)
    var senderRole: SenderRole,

    @Column(columnDefinition = "text", nullable = false)
    var body: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
