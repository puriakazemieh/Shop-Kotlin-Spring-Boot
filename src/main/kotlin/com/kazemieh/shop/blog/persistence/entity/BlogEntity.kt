package com.kazemieh.shop.blog.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "blogs")
class BlogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var title: String = "",

    @Column(nullable = false, unique = true)
    var slug: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "", // JSON structure for block-based editor

    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    @Column(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @Column(name = "view_count")
    var viewCount: Long = 0,

    @Column(name = "reading_time_minutes")
    var readingTimeMinutes: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BlogStatus = BlogStatus.DRAFT,

    @Column(name = "author_id")
    var authorId: Long? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

enum class BlogStatus {
    DRAFT, PUBLISHED
}
