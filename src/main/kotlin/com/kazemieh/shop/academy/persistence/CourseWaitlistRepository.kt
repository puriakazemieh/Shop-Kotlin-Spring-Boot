package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.CourseWaitlistEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseWaitlistRepository : JpaRepository<CourseWaitlistEntity, Long> {
    fun findByCourseIdAndUserId(courseId: Long, userId: Long): CourseWaitlistEntity?
    fun existsByCourseIdAndUserIdAndNotifiedFalse(courseId: Long, userId: Long): Boolean
    fun findAllByCourseIdOrderByCreatedAtAsc(courseId: Long): List<CourseWaitlistEntity>
    fun findAllByCourseIdAndNotifiedFalseOrderByCreatedAtAsc(courseId: Long): List<CourseWaitlistEntity>
    fun countByCourseIdAndNotifiedFalse(courseId: Long): Long
}
