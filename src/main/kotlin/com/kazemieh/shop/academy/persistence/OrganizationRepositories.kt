package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.OrganizationEntity
import com.kazemieh.shop.academy.persistence.entity.OrganizationSeatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrganizationRepository : JpaRepository<OrganizationEntity, Long>

@Repository
interface OrganizationSeatRepository : JpaRepository<OrganizationSeatEntity, Long> {
    fun findAllByOrganizationIdOrderByIdAsc(organizationId: Long): List<OrganizationSeatEntity>
    fun findAllByOrganizationIdAndCourseIdAndAssignedUserIdIsNull(organizationId: Long, courseId: Long): List<OrganizationSeatEntity>
    fun countByOrganizationIdAndCourseId(organizationId: Long, courseId: Long): Long
}
