package com.kazemieh.shop.shared.config

import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.LessonQuizRepository
import com.kazemieh.shop.academy.persistence.QuizRepository
import com.kazemieh.shop.academy.persistence.entity.*
import com.kazemieh.shop.clinic.persistence.AppointmentRepository
import com.kazemieh.shop.clinic.persistence.AvailabilitySlotRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.*
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.psychtest.persistence.PsychTestRepository
import com.kazemieh.shop.psychtest.persistence.UserPsychTestRepository
import com.kazemieh.shop.psychtest.persistence.entity.*
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * داده‌ی نمونه برای عمودی‌های آکادمی/کلینیک/تست — جدا از [DataSeeder] تا مستقل و
 * ایمن (idempotent per-vertical) اجرا شود. هر عمودی فقط وقتی seed می‌شود که خالی باشد.
 *
 * دوره‌ها با بخش/درس/ویدیو/فایل/آزمونِ درس/آزمونِ پایانی ساخته می‌شوند و مشتریِ نمونه
 * (user@carmilla.test) در چند مورد ثبت‌نام/خرید می‌شود تا کلِ مسیر قابلِ آزمایش باشد.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = ["app.seed.enabled"], havingValue = "true")
class VerticalDataSeeder(
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val quizRepository: QuizRepository,
    private val lessonQuizRepository: LessonQuizRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val therapistRepository: TherapistRepository,
    private val availabilitySlotRepository: AvailabilitySlotRepository,
    private val appointmentRepository: AppointmentRepository,
    private val psychTestRepository: PsychTestRepository,
    private val userPsychTestRepository: UserPsychTestRepository,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(VerticalDataSeeder::class.java)

    @Transactional
    override fun run(vararg args: String) {
        val customerId = userRepository.findByEmail("user@carmilla.test")?.id

        // به‌صورتِ per-slug اضافه می‌شود تا حتی اگر کاربر دوره/تستِ دستیِ دیگری ساخته باشد،
        // داده‌ی نمونه همچنان (idempotent) ایجاد شود.
        seedAcademy(customerId)
        seedClinic(customerId)
        seedPsychTests(customerId)
    }

    // ------------------------------------------------------------------ آکادمی
    private fun seedAcademy(customerId: Long?) {
        if (!courseRepository.existsBySlug("anxiety-management")) seedCourse1(customerId)
        if (!courseRepository.existsBySlug("communication-skills-workshop")) seedCourse2()
        if (!courseRepository.existsBySlug("mindfulness-intro-free")) seedCourse3()
    }

    private fun seedCourse1(customerId: Long?) {
        log.info("Seeding academy sample data…")

        // ---- دوره‌ی ۱: آنلاینِ ضبط‌شده با ویدیو/فایل/آزمونِ درس/آزمونِ پایانی ----
        val course1 = CourseEntity(
            title = "دوره جامع مدیریت اضطراب",
            slug = "anxiety-management",
            description = "یک دوره‌ی کامل برای شناخت و مدیریت اضطراب با تمرین‌های عملی و ویدیوهای گام‌به‌گام.",
            thumbnailUrl = "https://picsum.photos/seed/anxiety/640/360",
            instructor = "دکتر امیر رستمی",
            instructorBio = "روان‌شناسِ بالینی با ۱۲ سال سابقه‌ی درمانِ اختلالاتِ اضطرابی.",
            instructorSkills = "درمان شناختی-رفتاری, ذهن‌آگاهی, مدیریت استرس",
            price = BigDecimal("1450000"),
            discountedPrice = BigDecimal("1160000"),
            courseType = CourseType.COURSE,
            format = CourseFormat.ONLINE_RECORDED,
            level = CourseLevel.BEGINNER,
            jobMarketBadge = true,
            freeUpdateBadge = true,
        )
        val s1 = CourseSectionEntity(course = course1, title = "مقدمه و آشنایی", sortOrder = 0)
        s1.lessons.add(
            LessonEntity(
                section = s1, title = "اضطراب چیست؟", sortOrder = 0, durationSeconds = 320,
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                isFreePreview = true,
                resourceFiles = mutableListOf(LessonFile(name = "جزوه‌ی جلسه‌ی اول.pdf", url = "https://example.com/files/anxiety-1.pdf", sizeLabel = "۱٫۲ مگابایت")),
            )
        )
        s1.lessons.add(
            LessonEntity(
                section = s1, title = "نشانه‌های جسمی و ذهنی", sortOrder = 1, durationSeconds = 410,
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                videoVariants = mutableListOf(
                    VideoVariant(quality = "480p", url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                    VideoVariant(quality = "720p", url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                ),
            )
        )
        val s2 = CourseSectionEntity(course = course1, title = "تکنیک‌های عملی", sortOrder = 1)
        s2.lessons.add(
            LessonEntity(
                section = s2, title = "تنفس دیافراگمی", sortOrder = 0, durationSeconds = 500,
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                resourceFiles = mutableListOf(LessonFile(name = "تمرین تنفس.pdf", url = "https://example.com/files/breathing.pdf", sizeLabel = "۸۰۰ کیلوبایت")),
            )
        )
        s2.lessons.add(
            LessonEntity(
                section = s2, title = "بازسازیِ شناختی", sortOrder = 1, durationSeconds = 620,
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            )
        )
        course1.sections.add(s1)
        course1.sections.add(s2)
        val savedCourse1 = courseRepository.save(course1)

        // آزمونِ درس روی درسِ دوم (پس از ذخیره، id درس‌ها ست شده)
        val secondLesson = savedCourse1.sections.first().lessons[1]
        lessonQuizRepository.save(
            LessonQuizEntity(
                lessonId = secondLesson.id,
                title = "آزمونِ کوتاهِ نشانه‌ها",
                passScore = 60,
                questions = mutableListOf(
                    QuizQuestion(
                        text = "کدام مورد از نشانه‌های جسمیِ اضطراب است؟",
                        options = mutableListOf(
                            QuizOption(text = "تپشِ قلب", correct = true),
                            QuizOption(text = "افزایشِ تمرکز", correct = false),
                            QuizOption(text = "کاهشِ ضربان", correct = false),
                        )
                    ),
                )
            )
        )

        // آزمونِ پایانِ دوره (قبولی → صدور گواهی)
        quizRepository.save(
            QuizEntity(
                courseId = savedCourse1.id,
                title = "آزمونِ پایانِ دوره‌ی مدیریت اضطراب",
                passScore = 70,
                questions = mutableListOf(
                    QuizQuestion(
                        text = "مؤثرترین تکنیکِ کوتاه‌مدت برای آرام‌سازی کدام است؟",
                        options = mutableListOf(
                            QuizOption(text = "تنفس دیافراگمی", correct = true),
                            QuizOption(text = "نوشیدنِ قهوه", correct = false),
                            QuizOption(text = "بی‌خوابی", correct = false),
                        )
                    ),
                    QuizQuestion(
                        text = "بازسازیِ شناختی یعنی…",
                        options = mutableListOf(
                            QuizOption(text = "تغییرِ افکارِ ناکارآمد", correct = true),
                            QuizOption(text = "اجتنابِ کامل از موقعیت", correct = false),
                        )
                    ),
                )
            )
        )

        // مشتریِ نمونه در دوره‌ی ۱ ثبت‌نام می‌شود تا مسیرِ «دوره‌های من» قابلِ آزمایش باشد
        if (customerId != null && !enrollmentRepository.existsByUserIdAndCourseId(customerId, savedCourse1.id)) {
            enrollmentRepository.save(EnrollmentEntity(userId = customerId, course = savedCourse1))
        }
    }

    private fun seedCourse2() {
        // ---- دوره‌ی ۲: کارگاهِ حضوری (مثلِ کالای رزروی، بدونِ پخشِ آنلاین) ----
        val course2 = CourseEntity(
            title = "کارگاه گروهی مهارت‌های ارتباطی",
            slug = "communication-skills-workshop",
            description = "کارگاهِ حضوریِ یک‌روزه برای تقویتِ مهارت‌های ارتباطیِ مؤثر.",
            thumbnailUrl = "https://picsum.photos/seed/comm/640/360",
            instructor = "دکتر لیلا کریمی",
            price = BigDecimal("790000"),
            courseType = CourseType.WORKSHOP,
            format = CourseFormat.IN_PERSON,
            level = CourseLevel.INTERMEDIATE,
            location = "تهران، مرکز مشاوره مهرجو",
            capacity = 20,
            seatsTaken = 6,
        )
        courseRepository.save(course2)
    }

    private fun seedCourse3() {
        // ---- دوره‌ی ۳: رایگانِ آنلاین با پیش‌نمایشِ رایگان ----
        val course3 = CourseEntity(
            title = "آشنایی با ذهن‌آگاهی (رایگان)",
            slug = "mindfulness-intro-free",
            description = "یک دوره‌ی کوتاهِ رایگان برای شروعِ تمرینِ ذهن‌آگاهی.",
            thumbnailUrl = "https://picsum.photos/seed/mindful/640/360",
            instructor = "دکتر امیر رستمی",
            price = BigDecimal.ZERO,
            courseType = CourseType.COURSE,
            format = CourseFormat.ONLINE_RECORDED,
            level = CourseLevel.BEGINNER,
        )
        val s31 = CourseSectionEntity(course = course3, title = "شروع", sortOrder = 0)
        s31.lessons.add(
            LessonEntity(
                section = s31, title = "ذهن‌آگاهی چیست؟", sortOrder = 0, durationSeconds = 240,
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                isFreePreview = true,
            )
        )
        course3.sections.add(s31)
        courseRepository.save(course3)
    }

    // ------------------------------------------------------------------ کلینیک
    private fun seedClinic(customerId: Long?) {
        if (therapistRepository.existsBySlug("amir-rostami")) return
        log.info("Seeding clinic sample data…")
        val now = OffsetDateTime.now()

        val t1 = therapistRepository.save(
            TherapistEntity(
                name = "دکتر امیر رستمی", slug = "amir-rostami",
                specialty = "روان‌شناسِ بالینی — اضطراب و افسردگی",
                bio = "۱۲ سال سابقه‌ی درمانِ اختلالاتِ اضطرابی و افسردگی.",
                photoUrl = "https://picsum.photos/seed/rostami/240/240",
                sessionPrice = BigDecimal("950000"), sessionDurationMinutes = 60,
                mode = SessionMode.ONLINE,
            )
        )
        val t2 = therapistRepository.save(
            TherapistEntity(
                name = "دکتر لیلا کریمی", slug = "leila-karimi",
                specialty = "مشاورِ خانواده و زوج‌درمانی",
                bio = "متخصصِ زوج‌درمانی و مشاوره‌ی خانواده.",
                photoUrl = "https://picsum.photos/seed/karimi/240/240",
                sessionPrice = BigDecimal("1100000"), sessionDurationMinutes = 60,
                mode = SessionMode.IN_PERSON, location = "تهران، مرکز مشاوره مهرجو",
            )
        )
        val t3 = therapistRepository.save(
            TherapistEntity(
                name = "دکتر حسین نوری", slug = "hossein-nouri",
                specialty = "مشاوره‌ی تلفنیِ فوری",
                bio = "مشاوره‌ی تلفنیِ فوری برای بحران‌های هیجانی.",
                photoUrl = "https://picsum.photos/seed/nouri/240/240",
                sessionPrice = BigDecimal("450000"), sessionDurationMinutes = 30,
                mode = SessionMode.PHONE,
            )
        )

        // بازه‌های زمانیِ آزاد برای هر درمانگر (چند روزِ آینده)
        for (therapist in listOf(t1, t2, t3)) {
            for (day in 1..3) {
                for (hour in listOf(10, 12, 16, 18)) {
                    val start = now.plusDays(day.toLong()).withHour(hour).withMinute(0).withSecond(0).withNano(0)
                    availabilitySlotRepository.save(
                        AvailabilitySlotEntity(
                            therapist = therapist,
                            startTime = start,
                            endTime = start.plusMinutes(therapist.sessionDurationMinutes.toLong()),
                        )
                    )
                }
            }
        }

        // نوبت‌های نمونه برای مشتری: یکی تأییدشده‌ی آینده و یکی برگزارشده‌ی گذشته
        if (customerId != null) {
            val confirmedSlot = availabilitySlotRepository.save(
                AvailabilitySlotEntity(
                    therapist = t1,
                    startTime = now.plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0),
                    endTime = now.plusDays(1).withHour(15).withMinute(0).withSecond(0).withNano(0),
                    isBooked = true, bookedCount = 1,
                )
            )
            appointmentRepository.save(
                AppointmentEntity(
                    userId = customerId, therapist = t1, slot = confirmedSlot,
                    status = AppointmentStatus.CONFIRMED,
                    videoRoomUrl = "https://meet.example.com/room/abc123",
                    notes = "جلسه‌ی اولِ ارزیابی.",
                )
            )
            val pastSlot = availabilitySlotRepository.save(
                AvailabilitySlotEntity(
                    therapist = t3,
                    startTime = now.minusDays(3).withHour(18).withMinute(0).withSecond(0).withNano(0),
                    endTime = now.minusDays(3).withHour(18).withMinute(30).withSecond(0).withNano(0),
                    isBooked = true, bookedCount = 1,
                )
            )
            appointmentRepository.save(
                AppointmentEntity(
                    userId = customerId, therapist = t3, slot = pastSlot,
                    status = AppointmentStatus.COMPLETED,
                    videoRoomUrl = "09120000000",
                )
            )
        }
    }

    // ------------------------------------------------------------------ تست‌ها
    private fun seedPsychTests(customerId: Long?) {
        if (psychTestRepository.existsBySlug("mbti")) return
        log.info("Seeding psych-test sample data…")

        val mbti = psychTestRepository.save(
            PsychTestEntity(
                title = "تست شخصیت‌شناسی MBTI",
                slug = "mbti",
                description = "شناختِ تیپِ شخصیتی بر اساسِ چهار شاخصِ اصلی.",
                price = BigDecimal("350000"),
                resultMode = TestResultMode.AUTO,
                questions = mutableListOf(
                    TestQuestion(
                        text = "در جمع‌ها بیشتر انرژی می‌گیرید یا در تنهایی؟",
                        options = mutableListOf(
                            TestOption(text = "در جمع", score = 0),
                            TestOption(text = "بیشتر در جمع", score = 1),
                            TestOption(text = "بیشتر در تنهایی", score = 2),
                            TestOption(text = "در تنهایی", score = 3),
                        )
                    ),
                    TestQuestion(
                        text = "در تصمیم‌گیری بیشتر به منطق تکیه می‌کنید یا احساس؟",
                        options = mutableListOf(
                            TestOption(text = "کاملاً منطق", score = 0),
                            TestOption(text = "بیشتر منطق", score = 1),
                            TestOption(text = "بیشتر احساس", score = 2),
                            TestOption(text = "کاملاً احساس", score = 3),
                        )
                    ),
                    TestQuestion(
                        text = "برنامه‌ریزی را ترجیح می‌دهید یا انعطاف؟",
                        options = mutableListOf(
                            TestOption(text = "برنامه‌ی دقیق", score = 0),
                            TestOption(text = "بیشتر برنامه", score = 1),
                            TestOption(text = "بیشتر انعطاف", score = 2),
                            TestOption(text = "کاملاً انعطاف", score = 3),
                        )
                    ),
                ),
                ranges = mutableListOf(
                    ScoreRange(minScore = 0, maxScore = 4, interpretation = "تیپِ برون‌گرا و ساختارمند."),
                    ScoreRange(minScore = 5, maxScore = 9, interpretation = "ترکیبی و انعطاف‌پذیر."),
                ),
            )
        )

        psychTestRepository.save(
            PsychTestEntity(
                title = "تست افسردگی و اضطراب Beck",
                slug = "beck-depression-anxiety",
                description = "غربالگریِ تخصصی که نتیجه‌ی آن توسطِ مشاور تفسیر می‌شود.",
                price = BigDecimal("250000"),
                resultMode = TestResultMode.COUNSELOR,
                questions = mutableListOf(
                    TestQuestion(
                        text = "در دو هفته‌ی گذشته چقدر احساسِ ناامیدی داشته‌اید؟",
                        options = mutableListOf(
                            TestOption(text = "اصلاً", score = 0),
                            TestOption(text = "کمی", score = 1),
                            TestOption(text = "زیاد", score = 2),
                            TestOption(text = "بسیار زیاد", score = 3),
                        )
                    ),
                ),
            )
        )

        // مشتریِ نمونه یک تست را خریده تا در «تست‌های من» دیده و انجام شود
        if (customerId != null) {
            userPsychTestRepository.save(
                UserPsychTestEntity(userId = customerId, testId = mbti.id, status = UserTestStatus.PURCHASED)
            )
        }
    }
}
