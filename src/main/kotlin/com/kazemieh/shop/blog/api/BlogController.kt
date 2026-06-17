package com.kazemieh.shop.blog.api

import com.kazemieh.shop.blog.api.dto.*
import com.kazemieh.shop.blog.application.BlogService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/blogs")
class BlogController(
    private val blogService: BlogService,
) {

    @GetMapping
    fun getPublishedBlogs(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoryId: Long?,
        pageable: Pageable
    ): Page<BlogSummaryResponse> {
        return blogService.getPublishedBlogs(search, categoryId, pageable)
    }

    @GetMapping("/featured")
    fun getFeaturedBlogs(pageable: Pageable): Page<BlogSummaryResponse> {
        return blogService.getFeaturedBlogs(pageable)
    }

    @GetMapping("/categories")
    fun getAllCategories(): List<BlogCategoryResponse> {
        return blogService.getAllCategories()
    }

    @GetMapping("/{slug}")
    fun getBlogBySlug(@PathVariable slug: String): BlogResponse {
        return blogService.getBlogBySlug(slug, isAdmin = false)
    }

    @GetMapping("/{slug}/related")
    fun getRelatedBlogs(@PathVariable slug: String): List<BlogSummaryResponse> {
        return blogService.getRelatedBlogs(slug)
    }
}

@RestController
@RequestMapping("/api/admin/blogs")
@PreAuthorize("hasRole('ADMIN')")
class AdminBlogController(
    private val blogService: BlogService,
) {

    @GetMapping
    fun getAllBlogs(pageable: Pageable): Page<BlogAdminSummaryResponse> {
        return blogService.getAllBlogsForAdmin(pageable)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBlog(@RequestBody request: BlogCreateRequest): BlogResponse {
        return blogService.createBlog(request)
    }

    @PutMapping("/{id}")
    fun updateBlog(@PathVariable id: Long, @RequestBody request: BlogUpdateRequest): BlogResponse {
        return blogService.updateBlog(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBlog(@PathVariable id: Long) {
        blogService.deleteBlog(id)
    }

    @GetMapping("/categories")
    fun getAllCategories(): List<BlogCategoryResponse> {
        return blogService.getAllCategories()
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(@RequestBody request: BlogCategoryCreateRequest): BlogCategoryResponse {
        return blogService.createCategory(request)
    }

    @PutMapping("/categories/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @RequestBody request: BlogCategoryUpdateRequest
    ): BlogCategoryResponse {
        return blogService.updateCategory(id, request)
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCategory(@PathVariable id: Long) {
        blogService.deleteCategory(id)
    }

    @GetMapping("/{slug}")
    fun getBlogBySlugForAdmin(@PathVariable slug: String): BlogResponse {
        return blogService.getBlogBySlug(slug, isAdmin = true)
    }
}
