package com.kazemieh.shop.blog.persistence

import com.kazemieh.shop.blog.persistence.entity.BlogEntity
import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BlogRepository : JpaRepository<BlogEntity, Long> {
    fun findBySlug(slug: String): Optional<BlogEntity>
    fun findByStatus(status: BlogStatus, pageable: Pageable): Page<BlogEntity>
    fun existsBySlug(slug: String): Boolean
}
