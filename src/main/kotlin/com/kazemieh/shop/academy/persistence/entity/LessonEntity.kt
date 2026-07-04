package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/** یک کیفیتِ پخشِ ویدیو (مثلاً 720p → url). به‌صورت JSON ذخیره می‌شود. */
class VideoVariant(
    var quality: String = "",
    var url: String = ""
)

/** یک فایلِ ضمیمه‌ی درس (جزوه/کدِ نمونه/...) — کنارِ ویدیو، نه به‌جایِ آن. */
class LessonFile(
    var name: String = "",
    var url: String = "",
    var sizeLabel: String? = null
)

@Entity
@Table(name = "course_lessons")
class LessonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    var section: CourseSectionEntity,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(name = "video_url", length = 500)
    var videoUrl: String? = null,

    /** کیفیت‌های جایگزینِ پخش (۳۶۰/۴۸۰/۷۲۰/۱۰۸۰). اگر خالی باشد، فقط videoUrl استفاده می‌شود. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "video_variants", columnDefinition = "jsonb")
    var videoVariants: MutableList<VideoVariant> = mutableListOf(),

    /** فایل‌های ضمیمه‌ی این درس (جزوه/کدِ نمونه/...) — کنارِ ویدیو نمایش داده می‌شوند. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_files", columnDefinition = "jsonb")
    var resourceFiles: MutableList<LessonFile> = mutableListOf(),

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int = 0,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    /** درسِ پیش‌نمایشِ رایگان (بدونِ نیاز به ثبت‌نام قابلِ تماشا). */
    @Column(name = "is_free_preview", nullable = false)
    var isFreePreview: Boolean = false
)
