package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<CategoryEntity, Long> {
    fun findBySlug(slug: String): CategoryEntity?
    fun findAllByParentIdOrderByNameAsc(parentId: Long?): List<CategoryEntity>
    fun existsBySlug(slug: String): Boolean
}