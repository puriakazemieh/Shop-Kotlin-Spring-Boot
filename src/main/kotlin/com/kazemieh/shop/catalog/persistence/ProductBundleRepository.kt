package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductBundleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductBundleRepository : JpaRepository<ProductBundleEntity, Long> {
    fun findAllByIsActiveTrueOrderByCreatedAtDesc(): List<ProductBundleEntity>
    fun findBySlug(slug: String): ProductBundleEntity?
    fun existsBySlug(slug: String): Boolean
    fun findAllByProductIdIn(productIds: Collection<Long>): List<ProductBundleEntity>
}
