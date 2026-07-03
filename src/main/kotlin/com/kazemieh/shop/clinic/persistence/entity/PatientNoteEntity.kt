package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

/**
 * یادداشتِ محرمانه‌ی مشاور درباره‌ی یک نوبت/مراجع. داده‌ی حساس است و دسترسی به آن باید
 * به‌سختی کنترل شود (فقط ادمین/مشاورِ همان جلسه). در نسخه‌ی واقعی RBACِ سخت‌گیرانه لازم است.
 */
@Entity
@Table(
    name = "patient_notes",
    indexes = [Index(name = "idx_patient_notes_appointment", columnList = "appointment_id")]
)
class PatientNoteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "appointment_id", nullable = false)
    var appointmentId: Long,

    @Column(name = "counselor_id", nullable = false)
    var counselorId: Long,

    @Column(nullable = false, columnDefinition = "text")
    var note: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
