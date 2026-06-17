package com.kazemieh.shop.blog.persistence

import com.kazemieh.shop.blog.persistence.entity.BlogEntity
import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface BlogRepository : JpaRepository<BlogEntity, Long>, JpaSpecificationExecutor<BlogEntity> {
    fun findBySlug(slug: String): Optional<BlogEntity>
    fun findByStatus(status: BlogStatus, pageable: Pageable): Page<BlogEntity>
    fun findByStatusAndTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
        status: BlogStatus,
        title: String,
        summary: String,
        pageable: Pageable
    ): Page<BlogEntity>

    fun findByStatusAndCategoryId(status: BlogStatus, categoryId: Long, pageable: Pageable): Page<BlogEntity>

    fun findByStatusAndIsFeaturedTrue(status: BlogStatus, pageable: Pageable): Page<BlogEntity>

    fun existsBySlug(slug: String): Boolean
    fun findTop3ByStatusAndIdNotOrderByCreatedAtDesc(status: BlogStatus, id: Long): List<BlogEntity>
}
