package com.kazemieh.shop.blog.api.dto

import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import java.time.LocalDateTime

data class BlogCreateRequest(
    val title: String,
    val content: String,
    val summary: String? = null,
    val thumbnailUrl: String? = null,
    val status: BlogStatus = BlogStatus.DRAFT
)

data class BlogUpdateRequest(
    val title: String?,
    val content: String?,
    val summary: String?,
    val thumbnailUrl: String?,
    val status: BlogStatus?
)

data class BlogResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val content: String,
    val summary: String?,
    val thumbnailUrl: String?,
    val viewCount: Long,
    val readingTimeMinutes: Int,
    val status: BlogStatus,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class BlogSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val summary: String?,
    val thumbnailUrl: String?,
    val viewCount: Long,
    val readingTimeMinutes: Int,
    val createdAt: LocalDateTime?
)

data class MediaUploadResponse(
    val url: String
)
