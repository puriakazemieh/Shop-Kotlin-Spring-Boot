package com.kazemieh.shop.blog.api.dto

import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import java.time.LocalDateTime

data class BlogBlock(
    val type: String, // "header", "paragraph", "image"
    val content: String, // For text, it's the text. For image, it's the URL.
    val level: Int? = null // For header (1, 2, 3)
)

data class BlogCreateRequest(
    val title: String,
    val content: List<BlogBlock>,
    val summary: String? = null,
    val thumbnailUrl: String? = null,
    val status: BlogStatus = BlogStatus.DRAFT,
    val categoryId: Long? = null,
    val isFeatured: Boolean = false,
    val metaTitle: String? = null,
    val metaDescription: String? = null
)

data class BlogUpdateRequest(
    val title: String? = null,
    val slug: String? = null,
    val content: List<BlogBlock>? = null,
    val summary: String? = null,
    val thumbnailUrl: String? = null,
    val status: BlogStatus? = null,
    val categoryId: Long? = null,
    val isFeatured: Boolean? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null
)

data class BlogResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val content: List<BlogBlock>,
    val summary: String?,
    val thumbnailUrl: String?,
    val viewCount: Long,
    val readingTimeMinutes: Int,
    val status: BlogStatus,
    val author: AuthorResponse?,
    val category: BlogCategoryResponse?,
    val isFeatured: Boolean,
    val metaTitle: String?,
    val metaDescription: String?,
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
    val authorName: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val categorySlug: String?,
    val isFeatured: Boolean,
    val createdAt: LocalDateTime?
)

data class AuthorResponse(
    val id: Long,
    val name: String
)

data class BlogCategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?
)

data class BlogCategoryCreateRequest(
    val name: String,
    val description: String? = null
)

data class BlogCategoryUpdateRequest(
    val name: String?,
    val description: String?
)

data class BlogAdminSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val status: BlogStatus,
    val authorName: String?,
    val categoryName: String?,
    val isFeatured: Boolean,
    val viewCount: Long,
    val createdAt: LocalDateTime?
)

data class MediaUploadResponse(
    val url: String
)
