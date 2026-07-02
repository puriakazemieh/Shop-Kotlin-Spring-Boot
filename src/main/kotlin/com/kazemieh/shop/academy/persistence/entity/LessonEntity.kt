package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*

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

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int = 0,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    /** درسِ پیش‌نمایشِ رایگان (بدونِ نیاز به ثبت‌نام قابلِ تماشا). */
    @Column(name = "is_free_preview", nullable = false)
    var isFreePreview: Boolean = false
)
