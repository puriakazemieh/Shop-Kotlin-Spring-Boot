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
    private val blogService: BlogService
) {

    @GetMapping
    fun getPublishedBlogs(pageable: Pageable): Page<BlogSummaryResponse> {
        return blogService.getPublishedBlogs(pageable)
    }

    @GetMapping("/{slug}")
    fun getBlogBySlug(@PathVariable slug: String): BlogResponse {
        return blogService.getBlogBySlug(slug)
    }
}

@RestController
@RequestMapping("/api/admin/blogs")
@PreAuthorize("hasRole('ADMIN')")
class AdminBlogController(
    private val blogService: BlogService
) {

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
}
