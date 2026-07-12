package com.kazemieh.shop.support.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

enum class TicketStatus { OPEN, ANSWERED, CLOSED }

@Entity
@Table(name = "support_tickets")
class SupportTicketEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(nullable = false, length = 200)
    var subject: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TicketStatus = TicketStatus.OPEN,

    @OneToMany(mappedBy = "ticket", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("createdAt ASC")
    var messages: MutableList<SupportMessageEntity> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
