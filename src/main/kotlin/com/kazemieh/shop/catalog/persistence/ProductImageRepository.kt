package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductImageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductImageRepository : JpaRepository<ProductImageEntity, Long> {
    fun findAllByProductIdInOrderBySortOrderAsc(productIds: List<Long>): List<ProductImageEntity>
    fun findAllByProductIdOrderBySortOrderAsc(productId: Long): List<ProductImageEntity>
    fun findTopByProductIdOrderBySortOrderDesc(productId: Long): ProductImageEntity?
}