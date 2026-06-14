package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductReviewRepository : JpaRepository<ProductReviewEntity, Long> {
    fun findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId: Long): List<ProductReviewEntity>
}
