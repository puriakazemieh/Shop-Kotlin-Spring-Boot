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
