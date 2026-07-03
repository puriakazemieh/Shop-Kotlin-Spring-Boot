package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.EnrollmentEntity
import com.kazemieh.shop.academy.persistence.entity.LessonEntity
import com.kazemieh.shop.academy.persistence.entity.LessonProgressEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<CourseEntity, Long> {
    fun findAllByIsPublishedTrueOrderByCreatedAtDesc(): List<CourseEntity>
    fun findBySlug(slug: String): CourseEntity?
    fun existsBySlug(slug: String): Boolean
    /** دوره‌هایی که به این محصولات لینک شده‌اند (برای اعطای دسترسی پس از خرید). */
    fun findAllByProductIdIn(productIds: Collection<Long>): List<CourseEntity>

    /** قفلِ ردیفِ دوره برای کنترلِ اتمیکِ ظرفیتِ کلاسِ حضوری هنگامِ ثبت‌نامِ همزمان. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseEntity c where c.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): CourseEntity?
}

@Repository
interface LessonRepository : JpaRepository<LessonEntity, Long>

@Repository
interface EnrollmentRepository : JpaRepository<EnrollmentEntity, Long> {
    fun findByUserIdAndCourseId(userId: Long, courseId: Long): EnrollmentEntity?
    fun existsByUserIdAndCourseId(userId: Long, courseId: Long): Boolean
    fun findAllByUserIdOrderByEnrolledAtDesc(userId: Long): List<EnrollmentEntity>
}

@Repository
interface LessonProgressRepository : JpaRepository<LessonProgressEntity, Long> {
    fun findByUserIdAndLessonId(userId: Long, lessonId: Long): LessonProgressEntity?
    fun findAllByUserIdAndCourseId(userId: Long, courseId: Long): List<LessonProgressEntity>
    fun countByUserIdAndCourseIdAndCompletedTrue(userId: Long, courseId: Long): Long
}
