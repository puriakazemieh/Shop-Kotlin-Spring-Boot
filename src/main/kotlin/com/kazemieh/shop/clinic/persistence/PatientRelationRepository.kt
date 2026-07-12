package com.kazemieh.shop.clinic.persistence

import com.kazemieh.shop.clinic.persistence.entity.PatientRelationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientRelationRepository : JpaRepository<PatientRelationEntity, Long> {
    fun findByTherapistIdAndUserId(therapistId: Long, userId: Long): PatientRelationEntity?
    fun findAllByTherapistId(therapistId: Long): List<PatientRelationEntity>
}
