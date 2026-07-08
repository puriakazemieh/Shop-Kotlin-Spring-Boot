package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*

/**
 * پرسشنامه‌ی تطبیقِ درمانگر: هر پرسش به یک تگ (مثلاً «اضطراب»، «رابطه») متصل است؛
 * تطبیق بی‌حالت است — سرور تگ‌هایِ انتخاب‌شده را با تخصصِ درمانگرها مقایسه می‌کند.
 */
@Entity
@Table(name = "therapist_match_questions")
class TherapistMatchQuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "question_text", columnDefinition = "text", nullable = false)
    var questionText: String,

    @Column(nullable = false, length = 100)
    var tag: String,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0
)
