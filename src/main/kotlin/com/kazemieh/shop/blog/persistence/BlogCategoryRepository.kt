package com.kazemieh.shop.blog.persistence

import com.kazemieh.shop.blog.persistence.entity.BlogCategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BlogCategoryRepository : JpaRepository<BlogCategoryEntity, Long> {
    fun findBySlug(slug: String): Optional<BlogCategoryEntity>
    fun existsBySlug(slug: String): Boolean
}
