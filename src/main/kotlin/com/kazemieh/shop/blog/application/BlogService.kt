package com.kazemieh.shop.blog.application

import com.kazemieh.shop.blog.api.dto.*
import com.kazemieh.shop.blog.persistence.BlogRepository
import com.kazemieh.shop.blog.persistence.entity.BlogEntity
import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import com.kazemieh.shop.shared.error.BlogNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BlogService(
    private val blogRepository: BlogRepository
) {

    fun getPublishedBlogs(pageable: Pageable): Page<BlogSummaryResponse> {
        return blogRepository.findByStatus(BlogStatus.PUBLISHED, pageable).map { it.toSummaryResponse() }
    }

    @Transactional
    fun getBlogBySlug(slug: String): BlogResponse {
        val blog = blogRepository.findBySlug(slug)
            .orElseThrow { BlogNotFoundException("Blog not found with slug: $slug") }
        
        blog.viewCount++ // Increment view count
        return blog.toResponse()
    }

    @Transactional
    fun createBlog(request: BlogCreateRequest): BlogResponse {
        val blog = BlogEntity(
            title = request.title,
            slug = generateSlug(request.title),
            content = request.content,
            summary = request.summary,
            thumbnailUrl = request.thumbnailUrl,
            status = request.status,
            readingTimeMinutes = calculateReadingTime(request.content)
        )
        return blogRepository.save(blog).toResponse()
    }

    @Transactional
    fun updateBlog(id: Long, request: BlogUpdateRequest): BlogResponse {
        val blog = blogRepository.findById(id)
            .orElseThrow { BlogNotFoundException("Blog not found with id: $id") }

        request.title?.let {
            blog.title = it
            blog.slug = generateSlug(it)
        }
        request.content?.let { 
            blog.content = it 
            blog.readingTimeMinutes = calculateReadingTime(it)
        }
        request.summary?.let { blog.summary = it }
        request.thumbnailUrl?.let { blog.thumbnailUrl = it }
        request.status?.let { blog.status = it }

        return blogRepository.save(blog).toResponse()
    }

    fun deleteBlog(id: Long) {
        if (!blogRepository.existsById(id)) {
            throw BlogNotFoundException("Blog not found with id: $id")
        }
        blogRepository.deleteById(id)
    }

    private fun generateSlug(title: String): String {
        val baseSlug = title.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
        
        var slug = baseSlug
        var counter = 1
        while (blogRepository.existsBySlug(slug)) {
            slug = "$baseSlug-${counter++}"
        }
        return slug
    }

    private fun calculateReadingTime(content: String): Int {
        // Basic estimation: average reading speed is 200 words per minute
        val words = content.split(Regex("\\s+")).size
        return (words / 200).coerceAtLeast(1)
    }

    private fun BlogEntity.toResponse() = BlogResponse(
        id = id,
        title = title,
        slug = slug,
        content = content,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        viewCount = viewCount,
        readingTimeMinutes = readingTimeMinutes,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BlogEntity.toSummaryResponse() = BlogSummaryResponse(
        id = id,
        title = title,
        slug = slug,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        viewCount = viewCount,
        readingTimeMinutes = readingTimeMinutes,
        createdAt = createdAt
    )
}
