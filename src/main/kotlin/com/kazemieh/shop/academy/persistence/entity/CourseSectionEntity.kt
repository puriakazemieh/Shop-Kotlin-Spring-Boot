package com.kazemieh.shop.academy.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "course_sections")
class CourseSectionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    var course: CourseEntity,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @OneToMany(mappedBy = "section", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    var lessons: MutableList<LessonEntity> = mutableListOf()
)
