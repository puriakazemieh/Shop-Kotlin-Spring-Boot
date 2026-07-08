package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.CourseRefundRequestEntity
import com.kazemieh.shop.academy.persistence.entity.RefundRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRefundRequestRepository : JpaRepository<CourseRefundRequestEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<CourseRefundRequestEntity>
    fun findAllByOrderByCreatedAtDesc(): List<CourseRefundRequestEntity>
    fun existsByUserIdAndCourseIdAndStatusNot(userId: Long, courseId: Long, status: RefundRequestStatus): Boolean
}
