package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.LessonProgressRepository
import com.kazemieh.shop.academy.persistence.LessonRepository
import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.EnrollmentEntity
import com.kazemieh.shop.academy.persistence.entity.LessonProgressEntity
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val progressRepository: LessonProgressRepository
) {

    @Transactional(readOnly = true)
    fun listCourses(userId: Long?): List<CourseSummaryResponse> =
        courseRepository.findAllByIsPublishedTrueOrderByCreatedAtDesc().map { it.toSummary(userId) }

    @Transactional(readOnly = true)
    fun getCourseDetail(slug: String, userId: Long?): CourseDetailResponse {
        val course = courseRepository.findBySlug(slug)
            ?: throw NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND)
        val enrolled = userId != null && enrollmentRepository.existsByUserIdAndCourseId(userId, course.id)
        val progressByLesson = if (userId != null)
            progressRepository.findAllByUserIdAndCourseId(userId, course.id).associateBy { it.lessonId }
        else emptyMap()

        val sections = course.sections.map { section ->
            SectionResponse(
                id = section.id,
                title = section.title,
                lessons = section.lessons.map { lesson ->
                    val canWatch = enrolled || lesson.isFreePreview
                    val p = progressByLesson[lesson.id]
                    LessonResponse(
                        id = lesson.id,
                        title = lesson.title,
                        durationSeconds = lesson.durationSeconds,
                        isFreePreview = lesson.isFreePreview,
                        videoUrl = if (canWatch) lesson.videoUrl else null,
                        completed = p?.completed ?: false,
                        lastPositionSeconds = p?.lastPositionSeconds ?: 0
                    )
                }
            )
        }
        val total = sections.sumOf { it.lessons.size }
        val completed = progressByLesson.values.count { it.completed }
        return course.toDetail(enrolled, sections, percent(completed, total))
    }

    @Transactional(readOnly = true)
    fun myCourses(userId: Long): List<CourseSummaryResponse> =
        enrollmentRepository.findAllByUserIdOrderByEnrolledAtDesc(userId)
            .map { it.course.toSummary(userId) }

    /**
     * ثبت‌نام در دوره. idempotent. در محصولِ واقعی باید پس از پرداختِ موفق فراخوانی شود
     * (یا از رویدادِ تکمیلِ سفارش)؛ اینجا مستقیم اجازه داده می‌شود.
     * برای دوره‌های حضوری/آفلاینِ دارایِ ظرفیت، صندلی به‌صورتِ اتمیک (با قفلِ ردیف) رزرو می‌شود
     * تا دو ثبت‌نامِ همزمان نتوانند از ظرفیت عبور کنند.
     */
    @Transactional
    fun enroll(userId: Long, courseId: Long): CourseDetailResponse {
        val course = courseRepository.findById(courseId)
            .orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            // فقط برای دوره‌های حضوری/آفلاینِ دارایِ ظرفیت، صندلی رزرو کن.
            if (!course.format.isOnline && course.capacity != null) {
                val locked = courseRepository.findByIdForUpdate(courseId)
                    ?: throw NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND)
                val cap = locked.capacity ?: Int.MAX_VALUE
                if (locked.seatsTaken >= cap) {
                    throw ConflictException("Class is full", ErrorCodes.COURSE_CLASS_FULL)
                }
                locked.seatsTaken += 1
                courseRepository.save(locked)
            }
            enrollmentRepository.save(EnrollmentEntity(userId = userId, course = course))
        }
        return getCourseDetail(course.slug, userId)
    }

    @Transactional
    fun updateLessonProgress(userId: Long, lessonId: Long, req: UpdateProgressRequest): ProgressResponse {
        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { NotFoundException("Lesson not found", ErrorCodes.LESSON_NOT_FOUND) }
        val courseId = lesson.section.course.id
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw ForbiddenException("Not enrolled in this course", ErrorCodes.NOT_ENROLLED)
        }
        val progress = progressRepository.findByUserIdAndLessonId(userId, lessonId)
            ?: LessonProgressEntity(userId = userId, lessonId = lessonId, courseId = courseId)
        req.completed?.let { progress.completed = it }
        req.lastPositionSeconds?.let { progress.lastPositionSeconds = it.coerceAtLeast(0) }
        progressRepository.save(progress)
        return getProgress(userId, courseId)
    }

    @Transactional(readOnly = true)
    fun getProgress(userId: Long, courseId: Long): ProgressResponse {
        val course = courseRepository.findById(courseId)
            .orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
        val total = course.sections.sumOf { it.lessons.size }
        val completed = progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId).toInt()
        return ProgressResponse(courseId, total, completed, percent(completed, total))
    }

    private fun CourseEntity.toSummary(userId: Long?): CourseSummaryResponse {
        val lessonCount = sections.sumOf { it.lessons.size }
        val enrolled = userId != null && enrollmentRepository.existsByUserIdAndCourseId(userId, id)
        return CourseSummaryResponse(
            id = id,
            title = title,
            slug = slug,
            thumbnailUrl = thumbnailUrl,
            instructor = instructor,
            price = price,
            discountedPrice = discountedPrice,
            lessonCount = lessonCount,
            enrolled = enrolled,
            courseType = courseType.name,
            format = format.name,
            isOnline = format.isOnline,
            level = level?.name,
            jobMarketBadge = jobMarketBadge,
            freeUpdateBadge = freeUpdateBadge
        )
    }

    private fun percent(done: Int, total: Int): Int =
        if (total <= 0) 0 else ((done * 100.0) / total).toInt().coerceIn(0, 100)
}

/** مپِ مشترکِ جزئیاتِ دوره (استفاده در سرویسِ عمومی و ادمین). */
internal fun CourseEntity.toDetail(
    enrolled: Boolean,
    sections: List<SectionResponse>,
    progressPercent: Int
): CourseDetailResponse = CourseDetailResponse(
    id = id,
    title = title,
    slug = slug,
    description = description,
    thumbnailUrl = thumbnailUrl,
    instructor = instructor,
    price = price,
    discountedPrice = discountedPrice,
    enrolled = enrolled,
    progressPercent = progressPercent,
    sections = sections,
    courseType = courseType.name,
    format = format.name,
    isOnline = format.isOnline,
    level = level?.name,
    location = location,
    capacity = capacity,
    seatsTaken = seatsTaken,
    seatsRemaining = capacity?.let { (it - seatsTaken).coerceAtLeast(0) },
    jobMarketBadge = jobMarketBadge,
    freeUpdateBadge = freeUpdateBadge,
    instructorBio = instructorBio,
    instructorSkills = instructorSkills?.split("،", ",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
)
