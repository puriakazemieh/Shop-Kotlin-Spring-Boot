package com.kazemieh.shop.clinic.persistence

import com.kazemieh.shop.clinic.persistence.entity.AppointmentEntity
import com.kazemieh.shop.clinic.persistence.entity.AvailabilitySlotEntity
import com.kazemieh.shop.clinic.persistence.entity.PatientNoteEntity
import com.kazemieh.shop.clinic.persistence.entity.SessionCreditEntity
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

    /** همه‌ی بازه‌های یک درمانگر (برای مدیریتِ ادمین). */
    fun findAllByTherapistIdOrderByStartTimeAsc(therapistId: Long): List<AvailabilitySlotEntity>
}

@Repository
interface AppointmentRepository : JpaRepository<AppointmentEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<AppointmentEntity>
    fun findByIdAndUserId(id: Long, userId: Long): AppointmentEntity?

    /** همه‌ی نوبت‌ها (برای مدیریتِ ادمین). */
    fun findAllByOrderByCreatedAtDesc(): List<AppointmentEntity>
}

@Repository
interface PatientNoteRepository : JpaRepository<PatientNoteEntity, Long> {
    fun findAllByAppointmentIdOrderByCreatedAtDesc(appointmentId: Long): List<PatientNoteEntity>
}

@Repository
interface SessionCreditRepository : JpaRepository<SessionCreditEntity, Long> {
    fun findByUserIdAndTherapistId(userId: Long, therapistId: Long): SessionCreditEntity?

    /** قفلِ ردیف برای مصرفِ اتمیکِ اعتبار هنگامِ رزرو. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SessionCreditEntity c where c.userId = :userId and c.therapistId = :therapistId")
    fun findByUserIdAndTherapistIdForUpdate(
        @Param("userId") userId: Long,
        @Param("therapistId") therapistId: Long
    ): SessionCreditEntity?
}
