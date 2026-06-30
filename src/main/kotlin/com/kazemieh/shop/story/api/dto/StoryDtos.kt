package com.kazemieh.shop.story.api.dto

import com.kazemieh.shop.story.persistence.entity.StoryMediaType
import java.time.OffsetDateTime

data class StoryResponse(
    val id: Long,
    val mediaUrl: String,
    val mediaType: StoryMediaType,
    val productId: Long?,
    val linkType: String,
    val categoryId: Long?,
    val blogSlug: String?,
    val title: String?,
    val createdAt: OffsetDateTime?
)

data class AdminCreateStoryRequest(
    val productId: Long? = null,
    val linkType: String = "NONE",
    val categoryId: Long? = null,
    val blogSlug: String? = null,
    val title: String? = null,
    val durationHours: Long = 24
)

data class AdminUpdateStoryRequest(
    val productId: Long? = null,
    val linkType: String? = null,
    val categoryId: Long? = null,
    val blogSlug: String? = null,
    val title: String? = null,
    val isActive: Boolean? = null
)
