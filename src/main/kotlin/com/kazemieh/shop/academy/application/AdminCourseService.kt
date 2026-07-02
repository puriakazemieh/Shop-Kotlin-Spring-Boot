package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.CourseSectionEntity
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
                lessonCount = it.sections.sumOf { s -> s.lessons.size }, enrolled = false
            )
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
            isPublished = req.isPublished
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
        courseRepository.save(c)
    }

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
