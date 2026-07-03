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

data class VideoVariantResponse(
    val quality: String,
    val url: String
)

data class LessonResponse(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    /** فقط برای درسِ پیش‌نمایش یا کاربرِ ثبت‌نام‌شده پر می‌شود. */
    val videoUrl: String?,
    val completed: Boolean = false,
    val lastPositionSeconds: Int = 0,
    /** کیفیت‌های جایگزین (فقط وقتی قابلِ تماشا باشد پر می‌شود). */
    val videoVariants: List<VideoVariantResponse> = emptyList()
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
    val isFreePreview: Boolean = false,
    val videoVariants: List<VideoVariantResponse> = emptyList()
)

// ---------- Quiz ----------
data class QuizOptionResponse(
    val text: String,
    /** فقط برای ادمین/پاسخ‌کلید پر می‌شود؛ در endpointِ عمومی حذف می‌شود. */
    val correct: Boolean? = null
)

data class QuizQuestionResponse(
    val index: Int,
    val text: String,
    val options: List<QuizOptionResponse>
)

data class QuizResponse(
    val courseId: Long,
    val title: String,
    val passScore: Int,
    val questions: List<QuizQuestionResponse>,
    val alreadyPassed: Boolean = false
)

/** پاسخ‌های کاربر: برای هر سؤال، ایندکسِ گزینه‌ی انتخابی. */
data class SubmitQuizRequest(
    val answers: Map<Int, Int> = emptyMap()
)

data class QuizResultResponse(
    val courseId: Long,
    val score: Int,
    val passed: Boolean,
    val passScore: Int,
    val certificateNumber: String? = null
)

data class AdminUpsertQuizRequest(
    val title: String = "آزمونِ پایانِ دوره",
    val passScore: Int = 60,
    val questions: List<QuizQuestionResponse> = emptyList()
)

// ---------- Certificate ----------
data class CertificateResponse(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val certNumber: String,
    val issuedAt: String,
    val userName: String? = null
)
