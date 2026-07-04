package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.CourseWaitlistRepository
import com.kazemieh.shop.academy.persistence.LessonQuizRepository
import com.kazemieh.shop.academy.persistence.LessonRepository
import com.kazemieh.shop.academy.persistence.ProjectSubmissionRepository
import com.kazemieh.shop.academy.persistence.QuizRepository
import com.kazemieh.shop.academy.persistence.entity.CourseEntity
import com.kazemieh.shop.academy.persistence.entity.CourseFormat
import com.kazemieh.shop.academy.persistence.entity.CourseLevel
import com.kazemieh.shop.academy.persistence.entity.CourseSectionEntity
import com.kazemieh.shop.academy.persistence.entity.CourseType
import com.kazemieh.shop.academy.persistence.entity.LessonEntity
import com.kazemieh.shop.academy.persistence.entity.LessonFile
import com.kazemieh.shop.academy.persistence.entity.LessonQuizEntity
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionEntity
import com.kazemieh.shop.academy.persistence.entity.ProjectSubmissionStatus
import com.kazemieh.shop.academy.persistence.entity.QuizEntity
import com.kazemieh.shop.academy.persistence.entity.QuizOption
import com.kazemieh.shop.academy.persistence.entity.QuizQuestion
import com.kazemieh.shop.academy.persistence.entity.VideoVariant
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.BadRequestException
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class AdminCourseService(
    private val courseRepository: CourseRepository,
    private val quizRepository: QuizRepository,
    private val waitlistRepository: CourseWaitlistRepository,
    private val lessonRepository: LessonRepository,
    private val lessonQuizRepository: LessonQuizRepository,
    private val projectSubmissionRepository: ProjectSubmissionRepository,
    private val userRepository: UserRepository,
    private val quizService: QuizService
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
        val allLessonIds = c.sections.flatMap { it.lessons.map { l -> l.id } }
        val lessonIdsWithQuiz = lessonQuizRepository.findAllByLessonIdIn(allLessonIds).map { it.lessonId }.toSet()
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
                        lastPositionSeconds = 0,
                        videoVariants = lesson.videoVariants.map { VideoVariantResponse(it.quality, it.url) },
                        resourceFiles = lesson.resourceFiles.map { LessonFileResponse(it.name, it.url, it.sizeLabel) },
                        hasQuiz = lessonIdsWithQuiz.contains(lesson.id)
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
            instructorSkills = req.instructorSkills,
            requiresProjectSubmission = req.requiresProjectSubmission
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
        req.requiresProjectSubmission?.let { c.requiresProjectSubmission = it }
        courseRepository.save(c)
    }

    // ---- لیستِ انتظارِ کلاسِ حضوری ----
    @Transactional(readOnly = true)
    fun listWaitlist(courseId: Long): List<AdminWaitlistEntryResponse> =
        waitlistRepository.findAllByCourseIdOrderByCreatedAtAsc(courseId).map {
            AdminWaitlistEntryResponse(
                id = it.id, userId = it.userId, notified = it.notified,
                createdAt = it.createdAt.toString(), notifiedAt = it.notifiedAt?.toString()
            )
        }

    /** اطلاع‌رسانیِ دستیِ نفرِ اولِ صفِ انتظار (وقتی ادمین صندلیِ آزادشده را به او اختصاص می‌دهد). */
    @Transactional
    fun notifyNextInWaitlist(courseId: Long): AdminNotifyNextResponse {
        val next = waitlistRepository.findAllByCourseIdAndNotifiedFalseOrderByCreatedAtAsc(courseId).firstOrNull()
            ?: return AdminNotifyNextResponse(found = false)
        next.notified = true
        next.notifiedAt = OffsetDateTime.now()
        waitlistRepository.save(next)
        return AdminNotifyNextResponse(
            found = true,
            entry = AdminWaitlistEntryResponse(
                id = next.id, userId = next.userId, notified = true,
                createdAt = next.createdAt.toString(), notifiedAt = next.notifiedAt.toString()
            )
        )
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
            videoVariants = req.videoVariants.map { VideoVariant(it.quality, it.url) }.toMutableList(),
            durationSeconds = req.durationSeconds,
            sortOrder = req.sortOrder,
            isFreePreview = req.isFreePreview
        )
        section.lessons.add(lesson)
        courseRepository.save(c)
        return lesson.id
    }

    // ---- آزمونِ پایانِ دوره (upsert) ----
    @Transactional(readOnly = true)
    fun getQuiz(courseId: Long): QuizResponse? {
        val quiz = quizRepository.findByCourseId(courseId) ?: return null
        return QuizResponse(
            courseId = courseId,
            title = quiz.title,
            passScore = quiz.passScore,
            questions = quiz.questions.mapIndexed { i, q ->
                QuizQuestionResponse(
                    index = i,
                    text = q.text,
                    options = q.options.map { QuizOptionResponse(it.text, it.correct) }
                )
            }
        )
    }

    @Transactional
    fun upsertQuiz(courseId: Long, req: AdminUpsertQuizRequest) {
        findCourse(courseId) // اطمینان از وجودِ دوره
        val quiz = quizRepository.findByCourseId(courseId) ?: QuizEntity(courseId = courseId)
        quiz.title = req.title
        quiz.passScore = req.passScore.coerceIn(0, 100)
        quiz.questions = req.questions.map { q ->
            QuizQuestion(
                text = q.text,
                options = q.options.map { QuizOption(it.text, it.correct == true) }.toMutableList()
            )
        }.toMutableList()
        quizRepository.save(quiz)
    }

    // ---- فایل‌های ضمیمه‌ی درس (کنارِ ویدیو) ----
    // نکته: چون resourceFiles ستونِ jsonb است، لیستِ جدید باید reassign شود (نه mutateِ درجا)
    // تا Hibernate تغییر را در dirty-checking تشخیص دهد — هم‌الگو با upsertQuiz بالا.
    @Transactional
    fun addLessonFile(courseId: Long, lessonId: Long, req: AdminAddLessonFileRequest): Int {
        val lesson = findLessonInCourse(courseId, lessonId)
        val newFile = LessonFile(name = req.name.trim(), url = req.url, sizeLabel = req.sizeLabel)
        lesson.resourceFiles = (lesson.resourceFiles + newFile).toMutableList()
        lessonRepository.save(lesson)
        return lesson.resourceFiles.size - 1
    }

    @Transactional
    fun deleteLessonFile(courseId: Long, lessonId: Long, index: Int) {
        val lesson = findLessonInCourse(courseId, lessonId)
        if (index in lesson.resourceFiles.indices) {
            lesson.resourceFiles = lesson.resourceFiles.filterIndexed { i, _ -> i != index }.toMutableList()
            lessonRepository.save(lesson)
        }
    }

    // ---- آزمونِ کوتاهِ درس (checkpoint) ----
    @Transactional(readOnly = true)
    fun getLessonQuiz(courseId: Long, lessonId: Long): LessonQuizResponse? {
        findLessonInCourse(courseId, lessonId)
        val quiz = lessonQuizRepository.findByLessonId(lessonId) ?: return null
        return LessonQuizResponse(
            lessonId = lessonId,
            title = quiz.title,
            passScore = quiz.passScore,
            questions = quiz.questions.mapIndexed { i, q ->
                QuizQuestionResponse(
                    index = i,
                    text = q.text,
                    options = q.options.map { QuizOptionResponse(it.text, it.correct) }
                )
            }
        )
    }

    @Transactional
    fun upsertLessonQuiz(courseId: Long, lessonId: Long, req: AdminUpsertLessonQuizRequest) {
        findLessonInCourse(courseId, lessonId)
        val quiz = lessonQuizRepository.findByLessonId(lessonId) ?: LessonQuizEntity(lessonId = lessonId)
        quiz.title = req.title
        quiz.passScore = req.passScore.coerceIn(0, 100)
        quiz.questions = req.questions.map { q ->
            QuizQuestion(
                text = q.text,
                options = q.options.map { QuizOption(it.text, it.correct == true) }.toMutableList()
            )
        }.toMutableList()
        lessonQuizRepository.save(quiz)
    }

    // ---- پروژه‌های پایانی (ارزیابیِ پروژه‌محور) ----
    @Transactional(readOnly = true)
    fun listProjectSubmissions(courseId: Long): List<ProjectSubmissionResponse> =
        projectSubmissionRepository.findAllByCourseIdOrderBySubmittedAtDesc(courseId).map { it.toResponse() }

    @Transactional
    fun reviewProjectSubmission(submissionId: Long, req: AdminReviewProjectRequest) {
        val submission = projectSubmissionRepository.findById(submissionId)
            .orElseThrow { NotFoundException("Project submission not found", ErrorCodes.PROJECT_SUBMISSION_NOT_FOUND) }
        val status = runCatching { ProjectSubmissionStatus.valueOf(req.status.trim().uppercase()) }
            .getOrElse { throw BadRequestException("Invalid status", ErrorCodes.INVALID_INPUT) }
        submission.status = status
        submission.mentorFeedback = req.mentorFeedback
        submission.reviewedAt = OffsetDateTime.now()
        projectSubmissionRepository.save(submission)
        if (status == ProjectSubmissionStatus.APPROVED) {
            quizService.tryIssueCertificateIfEligible(submission.userId, submission.courseId)
        }
    }

    private fun ProjectSubmissionEntity.toResponse(): ProjectSubmissionResponse {
        val user = userRepository.findById(userId).orElse(null)
        val name = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { user?.email ?: "کاربر" }
        return ProjectSubmissionResponse(
            id = id, courseId = courseId, userId = userId, fileUrl = fileUrl, note = note,
            status = status.name, mentorFeedback = mentorFeedback,
            submittedAt = submittedAt.toString(), reviewedAt = reviewedAt?.toString(), userName = name
        )
    }

    private fun findLessonInCourse(courseId: Long, lessonId: Long): LessonEntity {
        val c = findCourse(courseId)
        val lesson = c.sections.flatMap { it.lessons }.firstOrNull { it.id == lessonId }
            ?: throw NotFoundException("Lesson not found", ErrorCodes.LESSON_NOT_FOUND)
        return lesson
    }

    private fun findCourse(id: Long): CourseEntity =
        courseRepository.findById(id).orElseThrow { NotFoundException("Course not found", ErrorCodes.COURSE_NOT_FOUND) }
}
