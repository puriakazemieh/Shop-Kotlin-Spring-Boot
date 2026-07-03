package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.CourseFormat
import com.kazemieh.shop.academy.persistence.entity.CourseLevel
import com.kazemieh.shop.academy.persistence.entity.CourseSectionEntity
import com.kazemieh.shop.academy.persistence.entity.CourseType
import com.kazemieh.shop.academy.persistence.entity.LessonEntity
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AdminCourseService(
    private val courseRepository: CourseRepository
) {

    @Transactional(readOnly = true)
    fun list(): List<CourseSummaryResponse> =
        courseRepository.findAll().map {
            CourseSummaryResponse(
                id = it.id, title = it.title, slug = it.slug, thumbnailUrl = it.thumbnailUrl,
                instructor = it.instructor, price = it.price, discountedPrice = it.discountedPrice,
                lessonCount = it.sections.sumOf { s -> s.lessons.size }, enrolled = false,
                courseType = it.courseType.name, format = it.format.name, isOnline = it.format.isOnline,
                level = it.level?.name, jobMarketBadge = it.jobMarketBadge, freeUpdateBadge = it.freeUpdateBadge
            )
        }

    /** جزئیاتِ کاملِ دوره برای ادمین — برخلافِ endpointِ عمومی، ویدیوها همیشه نمایان‌اند (بدونِ گیتِ ثبت‌نام). */
    @Transactional(readOnly = true)
    fun getDetail(id: Long): CourseDetailResponse {
        val c = findCourse(id)
        val sections = c.sections.map { section ->
            SectionResponse(
                id = section.id,
                title = section.title,
                lessons = section.lessons.map { lesson ->
                    LessonResponse(
                        id = lesson.id,
                        title = lesson.title,
                        durationSeconds = lesson.durationSeconds,
                        isFreePreview = lesson.isFreePreview,
                        videoUrl = lesson.videoUrl,
                        completed = false,
                        lastPositionSeconds = 0
                    )
                }
            )
        }
        return c.toDetail(enrolled = false, sections = sections, progressPercent = 0)
    }

    @Transactional
    fun create(req: AdminCreateCourseRequest): Long {
        val slug = req.slug.trim()
        if (courseRepository.existsBySlug(slug)) throw ConflictException("Course slug exists", ErrorCodes.COURSE_SLUG_EXISTS)
        val course = CourseEntity(
            title = req.title.trim(),
            slug = slug,
            description = req.description,
            thumbnailUrl = req.thumbnailUrl,
            instructor = req.instructor,
            price = req.price,
            discountedPrice = req.discountedPrice,
            productId = req.productId,
            isPublished = req.isPublished,
            courseType = parseType(req.courseType),
            format = parseFormat(req.format),
            level = parseLevel(req.level),
            location = req.location,
            capacity = req.capacity,
            jobMarketBadge = req.jobMarketBadge,
            freeUpdateBadge = req.freeUpdateBadge,
            instructorBio = req.instructorBio,
            instructorSkills = req.instructorSkills
        )
        return courseRepository.save(course).id
    }

    @Transactional
    fun update(id: Long, req: AdminUpdateCourseRequest) {
        val c = findCourse(id)
        req.title?.let { c.title = it.trim() }
        req.description?.let { c.description = it }
        req.thumbnailUrl?.let { c.thumbnailUrl = it }
        req.instructor?.let { c.instructor = it }
        req.price?.let { c.price = it }
        req.discountedPrice?.let { c.discountedPrice = it }
        req.isPublished?.let { c.isPublished = it }
        req.courseType?.let { c.courseType = parseType(it) }
        req.format?.let { c.format = parseFormat(it) }
        req.level?.let { c.level = parseLevel(it) }
        req.location?.let { c.location = it }
        req.capacity?.let { c.capacity = it }
        req.jobMarketBadge?.let { c.jobMarketBadge = it }
        req.freeUpdateBadge?.let { c.freeUpdateBadge = it }
        req.instructorBio?.let { c.instructorBio = it }
        req.instructorSkills?.let { c.instructorSkills = it }
        courseRepository.save(c)
    }

    private fun parseType(v: String?): CourseType =
        runCatching { CourseType.valueOf(v!!.trim().uppercase()) }.getOrDefault(CourseType.COURSE)

    private fun parseFormat(v: String?): CourseFormat =
        runCatching { CourseFormat.valueOf(v!!.trim().uppercase()) }.getOrDefault(CourseFormat.ONLINE_RECORDED)

    private fun parseLevel(v: String?): CourseLevel? =
        v?.takeIf { it.isNotBlank() }?.let { runCatching { CourseLevel.valueOf(it.trim().uppercase()) }.getOrNull() }

    @Transactional
    fun delete(id: Long) {
        val c = findCourse(id)
        courseRepository.delete(c)
    }

    @Transactional
    fun addSection(courseId: Long, req: AdminCreateSectionRequest): Long {
        val c = findCourse(courseId)
        val section = CourseSectionEntity(course = c, title = req.title.trim(), sortOrder = req.sortOrder)
        c.sections.add(section)
        courseRepository.save(c)
        return section.id
    }

    @Transactional
    fun addLesson(courseId: Long, sectionId: Long, req: AdminCreateLessonRequest): Long {
        val c = findCourse(courseId)
        val section = c.sections.firstOrNull { it.id == sectionId }
            ?: throw NotFoundException("Section not found", ErrorCodes.SECTION_NOT_FOUND)
        val lesson = LessonEntity(
            section = section,
            title = req.title.trim(),
            videoUrl = req.videoUrl,
            durationSeconds = req.durationSeconds,
            sortOrder = req.sortOrder,
            isFreePreview = req.isFreePreview
        )
        section.lessons.add(lesson)
        courseRepository.save(c)
        return lesson.id
    }

    private fun findCourse(id: Long): CourseEntity =
        courseRepository.findById(id).orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
}
