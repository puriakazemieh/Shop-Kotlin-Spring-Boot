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
    val completedLessons: Int = 0,
    val progressPercent: Int = 0,
    val enrolled: Boolean = false,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val isOnline: Boolean = true,
    val level: String? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val hasUnseenUpdate: Boolean = false
)

data class VideoVariantResponse(
    val quality: String,
    val url: String
)

data class LessonFileResponse(
    val name: String,
    val url: String,
    val sizeLabel: String? = null
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
    val videoVariants: List<VideoVariantResponse> = emptyList(),
    /** فایل‌های ضمیمه‌ی این درس (فقط وقتی قابلِ تماشا باشد پر می‌شود). */
    val resourceFiles: List<LessonFileResponse> = emptyList(),
    /** آیا این درس آزمونِ کوتاهِ خودش را دارد (برای نمایشِ تبِ «آزمون» در پخش‌کننده). */
    val hasQuiz: Boolean = false,
    /** زیرنویس‌های چندزبانه‌ی این درس. */
    val subtitles: List<SubtitleTrackResponse> = emptyList()
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
    val instructorSkills: List<String> = emptyList(),
    /** آیا ظرفیتِ کلاسِ حضوری تکمیل شده (فقط برای حضوری/آفلاینِ دارایِ ظرفیت). */
    val isFull: Boolean = false,
    /** آیا کاربرِ لاگین‌شده در لیستِ انتظار است (هنوز مطلع نشده). */
    val onWaitlist: Boolean = false,
    /** لینکِ محصولِ فروشگاه (اگر باشد) — برای نمایشِ بخشِ نظراتِ همان محصول با برچسبِ «نظرِ شاگردان». */
    val productId: Long? = null,
    /** آیا صدورِ گواهی نیازمندِ تأییدِ پروژه‌ی پایانی هم هست (کنارِ قبولیِ آزمون). */
    val requiresProjectSubmission: Boolean = false,
    /** کدِ تخفیفِ اختصاصیِ مدرس (اگر ادمین تنظیم کرده باشد). */
    val instructorDiscountCode: String? = null,
    /** جعبه‌ی «این دوره شامل چیست» — مجموعِ مدتِ ویدیوها و تعدادِ فایل‌های ضمیمه. */
    val totalDurationSeconds: Int = 0,
    val resourceFileCount: Int = 0,
    val hasUnseenUpdate: Boolean = false,
    /** برایِ دوره‌های همگروهی/زنده: تاریخِ شروعِ گروه. */
    val cohortStartDate: String? = null
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

// ---------- Waitlist (کلاسِ حضوریِ پرشده) ----------
data class WaitlistResponse(
    val courseId: Long,
    val joined: Boolean,
    /** جایگاهِ کاربر در صفِ انتظار (۱ = نفرِ بعدی). null یعنی از قبل مطلع شده یا عضو نیست. */
    val position: Int? = null
)

data class AdminWaitlistEntryResponse(
    val id: Long,
    val userId: Long,
    val notified: Boolean,
    val createdAt: String,
    val notifiedAt: String? = null
)

data class AdminNotifyNextResponse(
    val found: Boolean,
    val entry: AdminWaitlistEntryResponse? = null
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
    val instructorSkills: String? = null,
    val requiresProjectSubmission: Boolean = false,
    val cohortStartDate: String? = null
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
    val instructorSkills: String? = null,
    val requiresProjectSubmission: Boolean? = null,
    val instructorDiscountCode: String? = null,
    val cohortStartDate: String? = null
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
    val videoVariants: List<VideoVariantResponse> = emptyList(),
    val subtitles: List<SubtitleTrackResponse> = emptyList()
)

data class AdminAddSubtitleRequest(
    val language: String,
    val url: String
)

data class AdminAddLessonFileRequest(
    val name: String,
    val url: String,
    val sizeLabel: String? = null
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
    val alreadyPassed: Boolean = false,
    /** اگر برای این دوره آزمونی تعریف نشده باشد false است (به‌جایِ خطای ۴۰۴). */
    val hasQuiz: Boolean = true
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

data class CertificateVerifyResponse(
    val valid: Boolean,
    val courseTitle: String? = null,
    val certNumber: String? = null,
    val issuedAt: String? = null
)

// ---------- Lesson quiz (checkpoint per lesson, separate from the course-final quiz) ----------
data class LessonQuizResponse(
    val lessonId: Long,
    val title: String,
    val passScore: Int,
    val questions: List<QuizQuestionResponse>,
    val alreadyPassed: Boolean = false
)

data class SubmitLessonQuizRequest(
    val answers: Map<Int, Int> = emptyMap()
)

data class LessonQuizResultResponse(
    val lessonId: Long,
    val score: Int,
    val passed: Boolean,
    val passScore: Int
)

data class AdminUpsertLessonQuizRequest(
    val title: String = "آزمونِ این درس",
    val passScore: Int = 60,
    val questions: List<QuizQuestionResponse> = emptyList()
)

/** wrapper به‌جای JSONِ نال‌بلِ خام (سازگار با safeApiCallRaw<reified T> کلاینت). */
data class AdminLessonQuizResponse(
    val found: Boolean,
    val quiz: LessonQuizResponse? = null
)

// ---------- Project-based assessment ----------
data class ProjectSubmissionResponse(
    val id: Long,
    val courseId: Long,
    val userId: Long,
    val fileUrl: String,
    val note: String?,
    val status: String,
    val mentorFeedback: String?,
    val submittedAt: String,
    val reviewedAt: String? = null,
    val userName: String? = null
)

data class PeerCommentResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val comment: String,
    val createdAt: String?
)

data class CreatePeerCommentRequest(
    val comment: String
)

data class SubmitProjectRequest(
    val fileUrl: String,
    val note: String? = null
)

data class AdminReviewProjectRequest(
    val status: String,
    val mentorFeedback: String? = null
)

/** wrapper به‌جای JSONِ نال‌بلِ خام (سازگار با safeApiCallRaw<reified T> کلاینت). */
data class MyProjectResponse(
    val found: Boolean,
    val submission: ProjectSubmissionResponse? = null
)

// ---------- زیرنویسِ درس (Phase W) ----------
data class SubtitleTrackResponse(
    val language: String,
    val url: String
)

// ---------- سازمان/صندلیِ سازمانی (Phase W) ----------
data class OrganizationResponse(
    val id: Long,
    val name: String,
    val contactEmail: String?,
    val createdAt: String?
)

data class CreateOrganizationRequest(
    val name: String,
    val contactEmail: String? = null
)

data class SeatResponse(
    val id: Long,
    val organizationId: Long,
    val courseId: Long,
    val assignedUserId: Long?,
    val assignedEmail: String?,
    val assignedAt: String?
)

data class BuySeatsRequest(
    val courseId: Long,
    val count: Int
)

data class AssignSeatRequest(
    val courseId: Long,
    val email: String
)

// ---------- گارانتیِ بازگشتِ وجهِ دوره (Phase W) ----------
data class CourseRefundRequestRequest(
    val reason: String? = null
)

data class CourseRefundRequestResponse(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val amount: BigDecimal,
    val reason: String?,
    val status: String,
    val adminNote: String?,
    val createdAt: String?,
    val resolvedAt: String?
)

data class AdminCourseRefundRequestResponse(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val userId: Long,
    val userName: String?,
    val amount: BigDecimal,
    val reason: String?,
    val status: String,
    val adminNote: String?,
    val createdAt: String?,
    val resolvedAt: String?
)

data class AdminReviewRefundRequest(
    val approve: Boolean,
    val adminNote: String? = null
)
