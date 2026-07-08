package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

/** ثبتِ روزانه‌ی خلق‌وخو (۱ تا ۵) برایِ رسمِ نمودارِ روند در طولِ زمان. */
@Entity
@Table(name = "mood_checkins")
class MoodCheckInEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "mood_score", nullable = false)
    var moodScore: Int,

    @Column(columnDefinition = "text")
    var note: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
