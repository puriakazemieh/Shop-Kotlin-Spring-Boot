package com.kazemieh.shop.clinic.persistence

import com.kazemieh.shop.clinic.persistence.entity.AppointmentEntity
import com.kazemieh.shop.clinic.persistence.entity.AvailabilitySlotEntity
import com.kazemieh.shop.clinic.persistence.entity.TherapistEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface TherapistRepository : JpaRepository<TherapistEntity, Long> {
    fun findAllByIsActiveTrueOrderByCreatedAtDesc(): List<TherapistEntity>
    fun findBySlug(slug: String): TherapistEntity?
    fun existsBySlug(slug: String): Boolean
    fun findAllByProductIdIn(productIds: Collection<Long>): List<TherapistEntity>
}

@Repository
interface AvailabilitySlotRepository : JpaRepository<AvailabilitySlotEntity, Long> {
    /** بازه‌های آزادِ آینده‌ی یک درمانگر (رزرونشده). */
    fun findAllByTherapistIdAndIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(
        therapistId: Long,
        after: OffsetDateTime
    ): List<AvailabilitySlotEntity>

    /** قفلِ ردیف برای رزروِ اتمیک (جلوگیری از رزروِ همزمانِ یک بازه). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AvailabilitySlotEntity s where s.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): AvailabilitySlotEntity?
}

@Repository
interface AppointmentRepository : JpaRepository<AppointmentEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<AppointmentEntity>
    fun findByIdAndUserId(id: Long, userId: Long): AppointmentEntity?
}
