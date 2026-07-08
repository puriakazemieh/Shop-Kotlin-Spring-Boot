package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** یادداشتِ روزانه‌ی خصوصیِ کاربر؛ به‌صورتِ اختیاری می‌تواند با یک درمانگر به‌اشتراک گذاشته شود. */
@Entity
@Table(
    name = "journal_entries",
    indexes = [Index(name = "idx_journal_entries_user", columnList = "user_id, created_at")]
)
class JournalEntryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(columnDefinition = "text", nullable = false)
    var content: String,

    @Column(name = "shared_with_therapist_id")
    var sharedWithTherapistId: Long? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
