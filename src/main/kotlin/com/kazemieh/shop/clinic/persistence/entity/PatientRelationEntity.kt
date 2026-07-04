package com.kazemieh.shop.clinic.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

/**
 * رابطه‌ی درمانگر–مراجع (کارتِ CRM). نگهدارنده‌ی برچسب‌های آزادِ ادمین/مشاور روی یک مراجع
 * (مثلاً «نیازمندِ پیگیری»)، مستقل از نوبت‌های تک‌تک. هر درمانگر–کاربر حداکثر یک ردیف دارد.
 */
@Entity
@Table(
    name = "patient_relations",
    uniqueConstraints = [UniqueConstraint(name = "ux_patient_relation_therapist_user", columnNames = ["therapist_id", "user_id"])]
)
class PatientRelationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "therapist_id", nullable = false)
    var therapistId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    var tags: MutableList<String> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
)
