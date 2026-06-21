package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductReviewEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductReviewRepository : JpaRepository<ProductReviewEntity, Long> {
    @Query("SELECT r FROM ProductReviewEntity r WHERE (:productId IS NULL OR r.product.id = :productId) AND (:isNew IS NULL OR r.isNew = :isNew)")
    fun findAllByFilters(productId: Long?, isNew: Boolean?, pageable: Pageable): Page<ProductReviewEntity>

    fun findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId: Long): List<ProductReviewEntity>

    fun deleteAllByProductId(productId: Long)
}
