package com.kazemieh.shop.story.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

enum class StoryMediaType {
    IMAGE, VIDEO
}

@Entity
@Table(name = "stories")
class StoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "media_url", nullable = false, length = 500)
    var mediaUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    var mediaType: StoryMediaType = StoryMediaType.IMAGE,

    @Column(name = "product_id")
    var productId: Long? = null,

    // columnDefinition carries a DEFAULT so that when Hibernate (ddl-auto=update) adds this
    // NOT NULL column to a stories table that already has rows, existing rows are backfilled
    // with 'NONE' instead of failing with "contains null values".
    @Column(name = "link_type", nullable = false, columnDefinition = "varchar(20) default 'NONE'")
    var linkType: String = "NONE",

    @Column(name = "category_id")
    var categoryId: Long? = null,

    @Column(name = "blog_slug", length = 200)
    var blogSlug: String? = null,

    @Column(length = 100)
    var title: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: OffsetDateTime,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)
