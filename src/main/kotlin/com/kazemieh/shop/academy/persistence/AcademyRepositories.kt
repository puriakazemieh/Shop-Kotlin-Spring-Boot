package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.EnrollmentEntity
import com.kazemieh.shop.academy.persistence.entity.LessonEntity
import com.kazemieh.shop.academy.persistence.entity.LessonProgressEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<CourseEntity, Long> {
    fun findAllByIsPublishedTrueOrderByCreatedAtDesc(): List<CourseEntity>
    fun findBySlug(slug: String): CourseEntity?
    fun existsBySlug(slug: String): Boolean
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
