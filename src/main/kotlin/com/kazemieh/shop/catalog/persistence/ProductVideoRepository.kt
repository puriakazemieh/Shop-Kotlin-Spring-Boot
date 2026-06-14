package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductVideoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductVideoRepository : JpaRepository<ProductVideoEntity, Long> {
    fun findAllByProductIdOrderBySortOrderAsc(productId: Long): List<ProductVideoEntity>
    fun findTopByProductIdOrderBySortOrderDesc(productId: Long): ProductVideoEntity?
}
