package com.kazemieh.shop.academy.api.dto

import java.math.BigDecimal

// ---------- Public catalog ----------
data class CourseSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val lessonCount: Int,
    val enrolled: Boolean = false,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val isOnline: Boolean = true,
    val level: String? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false
)

data class LessonResponse(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    /** فقط برای درسِ پیش‌نمایش یا کاربرِ ثبت‌نام‌شده پر می‌شود. */
    val videoUrl: String?,
    val completed: Boolean = false,
    val lastPositionSeconds: Int = 0
)

data class SectionResponse(
    val id: Long,
    val title: String,
    val lessons: List<LessonResponse>
)

data class CourseDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val enrolled: Boolean,
    val progressPercent: Int,
    val sections: List<SectionResponse>,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val isOnline: Boolean = true,
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val seatsTaken: Int = 0,
    val seatsRemaining: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: List<String> = emptyList()
)

// ---------- Progress ----------
data class UpdateProgressRequest(
    val completed: Boolean? = null,
    val lastPositionSeconds: Int? = null
)

data class ProgressResponse(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercent: Int
)

// ---------- Admin ----------
data class AdminCreateCourseRequest(
    val title: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: BigDecimal = BigDecimal.ZERO,
    val discountedPrice: BigDecimal? = null,
    val productId: Long? = null,
    val isPublished: Boolean = true,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: String? = null
)

data class AdminUpdateCourseRequest(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: BigDecimal? = null,
    val discountedPrice: BigDecimal? = null,
    val isPublished: Boolean? = null,
    val courseType: String? = null,
    val format: String? = null,
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val jobMarketBadge: Boolean? = null,
    val freeUpdateBadge: Boolean? = null,
    val instructorBio: String? = null,
    val instructorSkills: String? = null
)

data class AdminCreateSectionRequest(
    val title: String,
    val sortOrder: Int = 0
)

data class AdminCreateLessonRequest(
    val title: String,
    val videoUrl: String? = null,
    val durationSeconds: Int = 0,
    val sortOrder: Int = 0,
    val isFreePreview: Boolean = false
)
