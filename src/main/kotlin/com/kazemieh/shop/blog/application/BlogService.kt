package com.kazemieh.shop.blog.application

import com.kazemieh.shop.blog.api.dto.*
import com.kazemieh.shop.blog.persistence.BlogCategoryRepository
import com.kazemieh.shop.blog.persistence.BlogRepository
import com.kazemieh.shop.blog.persistence.entity.BlogCategoryEntity
import com.kazemieh.shop.blog.persistence.entity.BlogEntity
import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.BlogNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BlogService(
    private val blogRepository: BlogRepository,
    private val blogCategoryRepository: BlogCategoryRepository,
    private val userRepository: UserRepository,
) {

    fun getPublishedBlogs(
        search: String?,
        categoryId: Long?,
        pageable: Pageable
    ): Page<BlogSummaryResponse> {
        val spec = Specification<BlogEntity> { root, _, cb ->
            val predicates = mutableListOf(
                cb.equal(root.get<BlogStatus>("status"), BlogStatus.PUBLISHED)
            )

            if (!search.isNullOrBlank()) {
                val searchLower = "%${search.lowercase()}%"
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("title")), searchLower),
                        cb.like(cb.lower(root.get("summary")), searchLower)
                    )
                )
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get<Long>("categoryId"), categoryId))
            }

            cb.and(*predicates.toTypedArray())
        }
        return blogRepository.findAll(spec, pageable).map { it.toSummaryResponse() }
    }

    fun getFeaturedBlogs(pageable: Pageable): Page<BlogSummaryResponse> {
        return blogRepository.findByStatusAndIsFeaturedTrue(BlogStatus.PUBLISHED, pageable)
            .map { it.toSummaryResponse() }
    }

    @Transactional
    fun getBlogBySlug(slug: String, isAdmin: Boolean = false): BlogResponse {
        val blog = blogRepository.findBySlug(slug)
            .orElseThrow { BlogNotFoundException("Blog not found with slug: $slug") }

        if (!isAdmin && (blog.status != BlogStatus.PUBLISHED)) {
            throw BlogNotFoundException("Blog not found with slug: $slug")
        }

        if (!isAdmin) {
            incrementViewCount(blog.id)
        }
        return blog.toBlogResponse()
    }

    @Async
    fun incrementViewCount(blogId: Long) {
        blogRepository.findById(blogId).ifPresent {
            it.viewCount++
            blogRepository.save(it)
        }
    }

    fun getRelatedBlogs(slug: String): List<BlogSummaryResponse> {
        val blog = blogRepository.findBySlug(slug)
            .orElseThrow { BlogNotFoundException("Blog not found with slug: $slug") }

        // Logic: Try to find latest in same category, then fallback to general latest
        val sameCategory = blog.categoryId?.let { catId ->
            blogRepository.findByStatusAndCategoryId(BlogStatus.PUBLISHED, catId, Pageable.ofSize(4))
                .content.filter { it.id != blog.id }.take(3)
        } ?: emptyList()

        if (sameCategory.size >= 3) return sameCategory.map { it.toSummaryResponse() }

        val others = blogRepository.findTop3ByStatusAndIdNotOrderByCreatedAtDesc(BlogStatus.PUBLISHED, blog.id)
            .filter { other -> sameCategory.none { it.id == other.id } }
            .take(3 - sameCategory.size)

        return (sameCategory + others).map { it.toSummaryResponse() }
    }

    // Category Methods
    fun getAllCategories(): List<BlogCategoryResponse> {
        return blogCategoryRepository.findAll().map { it.toCategoryResponse() }
    }

    fun getAllBlogsForAdmin(pageable: Pageable): Page<BlogAdminSummaryResponse> {
        return blogRepository.findAll(pageable).map { it.toAdminSummaryResponse() }
    }

    @Transactional
    fun createCategory(request: BlogCategoryCreateRequest): BlogCategoryResponse {
        val category = BlogCategoryEntity(
            name = request.name,
            slug = generateCategorySlug(request.name),
            description = request.description
        )
        return blogCategoryRepository.save(category).toCategoryResponse()
    }

    @Transactional
    fun updateCategory(id: Long, request: BlogCategoryUpdateRequest): BlogCategoryResponse {
        val category = blogCategoryRepository.findById(id)
            .orElseThrow { BlogNotFoundException("Category not found with id: $id") }
        
        request.name?.let { 
            category.name = it 
            category.slug = generateCategorySlug(it)
        }
        request.description?.let { category.description = it }
        
        return blogCategoryRepository.save(category).toCategoryResponse()
    }

    fun deleteCategory(id: Long) {
        if (!blogCategoryRepository.existsById(id)) {
            throw BlogNotFoundException("Category not found with id: $id")
        }
        blogCategoryRepository.deleteById(id)
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
            categoryId = request.categoryId,
            isFeatured = request.isFeatured,
            metaTitle = request.metaTitle,
            metaDescription = request.metaDescription,
            readingTimeMinutes = calculateReadingTime(request.content)
        )
        return blogRepository.save(blog).toBlogResponse()
    }

    @Transactional
    fun updateBlog(id: Long, request: BlogUpdateRequest): BlogResponse {
        val blog = blogRepository.findById(id)
            .orElseThrow { BlogNotFoundException("Blog not found with id: $id") }

        request.title?.let { blog.title = it }
        
        // Manual slug update or auto-generate if title changed and no slug provided
        if (request.slug != null) {
            blog.slug = validateAndFinalizeSlug(request.slug, blog.id)
        } else if (request.title != null) {
             blog.slug = generateSlug(request.title, blog.id)
        }

        request.content?.let {
            blog.content = it
            blog.readingTimeMinutes = calculateReadingTime(it)
        }
        request.summary?.let { blog.summary = it }
        request.thumbnailUrl?.let { blog.thumbnailUrl = it }
        request.status?.let { blog.status = it }
        request.categoryId?.let { blog.categoryId = it }
        request.isFeatured?.let { blog.isFeatured = it }
        request.metaTitle?.let { blog.metaTitle = it }
        request.metaDescription?.let { blog.metaDescription = it }

        return blogRepository.save(blog).toBlogResponse()
    }

    fun deleteBlog(id: Long) {
        if (!blogRepository.existsById(id)) {
            throw BlogNotFoundException("Blog not found with id: $id")
        }
        blogRepository.deleteById(id)
    }

    private fun generateSlug(title: String, currentId: Long? = null): String {
        val baseSlug = title.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9\\u0600-\\u06FF\\s]"), "") // Support Persian chars
            .replace(Regex("\\s+"), "-")
            .trim('-')

        return validateAndFinalizeSlug(baseSlug, currentId)
    }

    private fun validateAndFinalizeSlug(baseSlug: String, currentId: Long? = null): String {
        var slug = baseSlug
        var counter = 1
        while (true) {
            val existing = blogRepository.findBySlug(slug)
            if (existing.isEmpty || existing.get().id == currentId) {
                break
            }
            slug = "$baseSlug-${counter++}"
        }
        return slug
    }

    private fun generateCategorySlug(name: String): String {
        val baseSlug = name.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9\\u0600-\\u06FF\\s]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')

        var slug = baseSlug
        var counter = 1
        while (blogCategoryRepository.existsBySlug(slug)) {
            slug = "$baseSlug-${counter++}"
        }
        return slug
    }

    private fun calculateReadingTime(blocks: List<BlogBlock>): Int {
        val textContent = blocks.filter { it.type == "paragraph" || it.type == "header" }
            .joinToString(" ") { it.content }
        val words = textContent.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return (words / 200).coerceAtLeast(1)
    }

    private fun BlogEntity.toBlogResponse(): BlogResponse {
        val author = authorId?.let { id ->
            userRepository.findById(id).map { AuthorResponse(it.id, "${it.firstName} ${it.lastName}") }.orElse(null)
        }
        val category = categoryId?.let { id ->
            blogCategoryRepository.findById(id).map { it.toCategoryResponse() }.orElse(null)
        }
        return BlogResponse(
            id = id,
            title = title,
            slug = slug,
            content = content,
            summary = summary,
            thumbnailUrl = thumbnailUrl,
            viewCount = viewCount,
            readingTimeMinutes = readingTimeMinutes,
            status = status,
            author = author,
            category = category,
            isFeatured = isFeatured,
            metaTitle = metaTitle,
            metaDescription = metaDescription,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun BlogEntity.toSummaryResponse(): BlogSummaryResponse {
        val authorName = authorId?.let { id ->
            userRepository.findById(id).map { "${it.firstName} ${it.lastName}" }.orElse(null)
        }
        val category = categoryId?.let { id ->
            blogCategoryRepository.findById(id).orElse(null)
        }
        return BlogSummaryResponse(
            id = id,
            title = title,
            slug = slug,
            summary = summary,
            thumbnailUrl = thumbnailUrl,
            viewCount = viewCount,
            readingTimeMinutes = readingTimeMinutes,
            authorName = authorName,
            categoryId = category?.id,
            categoryName = category?.name,
            categorySlug = category?.slug,
            isFeatured = isFeatured,
            createdAt = createdAt
        )
    }

    private fun BlogEntity.toAdminSummaryResponse(): BlogAdminSummaryResponse {
        val authorName = authorId?.let { id ->
            userRepository.findById(id).map { "${it.firstName} ${it.lastName}" }.orElse(null)
        }
        val categoryName = categoryId?.let { id ->
            blogCategoryRepository.findById(id).map { it.name }.orElse(null)
        }
        return BlogAdminSummaryResponse(
            id = id,
            title = title,
            slug = slug,
            status = status,
            authorName = authorName,
            categoryName = categoryName,
            isFeatured = isFeatured,
            viewCount = viewCount,
            createdAt = createdAt
        )
    }

    private fun BlogCategoryEntity.toCategoryResponse() = BlogCategoryResponse(
        id = id,
        name = name,
        slug = slug,
        description = description
    )
}
