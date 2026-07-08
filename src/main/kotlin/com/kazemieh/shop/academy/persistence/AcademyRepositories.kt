package com.kazemieh.shop.academy.persistence

import com.kazemieh.shop.academy.persistence.entity.CertificateEntity
import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.EnrollmentEntity
import com.kazemieh.shop.academy.persistence.entity.LessonEntity
import com.kazemieh.shop.academy.persistence.entity.LessonProgressEntity
import com.kazemieh.shop.academy.persistence.entity.LessonQuizAttemptEntity
import com.kazemieh.shop.academy.persistence.entity.LessonQuizEntity
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionEntity
import com.kazemieh.shop.academy.persistence.entity.QuizAttemptEntity
import com.kazemieh.shop.academy.persistence.entity.QuizEntity
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
    fun findAllByCourseId(courseId: Long): List<EnrollmentEntity>
    fun deleteByUserIdAndCourseId(userId: Long, courseId: Long)
}

@Repository
interface LessonProgressRepository : JpaRepository<LessonProgressEntity, Long> {
    fun findByUserIdAndLessonId(userId: Long, lessonId: Long): LessonProgressEntity?
    fun findAllByUserIdAndCourseId(userId: Long, courseId: Long): List<LessonProgressEntity>
    fun countByUserIdAndCourseIdAndCompletedTrue(userId: Long, courseId: Long): Long
}

@Repository
interface QuizRepository : JpaRepository<QuizEntity, Long> {
    fun findByCourseId(courseId: Long): QuizEntity?
}

@Repository
interface QuizAttemptRepository : JpaRepository<QuizAttemptEntity, Long> {
    fun findAllByUserIdAndCourseIdOrderByCreatedAtDesc(userId: Long, courseId: Long): List<QuizAttemptEntity>
    fun existsByUserIdAndCourseIdAndPassedTrue(userId: Long, courseId: Long): Boolean
}

@Repository
interface CertificateRepository : JpaRepository<CertificateEntity, Long> {
    fun findByUserIdAndCourseId(userId: Long, courseId: Long): CertificateEntity?
    fun findAllByUserIdOrderByIssuedAtDesc(userId: Long): List<CertificateEntity>
    fun findByCertNumber(certNumber: String): CertificateEntity?
}

@Repository
interface LessonQuizRepository : JpaRepository<LessonQuizEntity, Long> {
    fun findByLessonId(lessonId: Long): LessonQuizEntity?
    fun findAllByLessonIdIn(lessonIds: Collection<Long>): List<LessonQuizEntity>
}

@Repository
interface LessonQuizAttemptRepository : JpaRepository<LessonQuizAttemptEntity, Long> {
    fun existsByUserIdAndLessonIdAndPassedTrue(userId: Long, lessonId: Long): Boolean
}

@Repository
interface ProjectSubmissionRepository : JpaRepository<ProjectSubmissionEntity, Long> {
    fun findByCourseIdAndUserId(courseId: Long, userId: Long): ProjectSubmissionEntity?
    fun findAllByCourseIdOrderBySubmittedAtDesc(courseId: Long): List<ProjectSubmissionEntity>
    fun existsByCourseIdAndUserIdAndStatus(courseId: Long, userId: Long, status: com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionStatus): Boolean
    fun findAllByCourseIdAndStatusOrderBySubmittedAtDesc(
        courseId: Long,
        status: com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionStatus
    ): List<ProjectSubmissionEntity>
}
