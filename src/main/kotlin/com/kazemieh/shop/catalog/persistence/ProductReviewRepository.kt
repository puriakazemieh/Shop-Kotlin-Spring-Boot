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

    /**
     * Batch aggregate of average rating and rated-review count per product.
     * Only top-level reviews (parent IS NULL) that carry a rating are counted.
     */
    @Query(
        """
        SELECT r.product.id AS productId,
               AVG(r.rating) AS avgRating,
               COUNT(r.id) AS reviewCount
        FROM ProductReviewEntity r
        WHERE r.product.id IN :productIds
          AND r.rating IS NOT NULL
          AND r.parent IS NULL
        GROUP BY r.product.id
        """
    )
    fun aggregateRatingsByProductIds(productIds: List<Long>): List<ProductRatingAggregate>
}

interface ProductRatingAggregate {
    fun getProductId(): Long
    fun getAvgRating(): Double?
    fun getReviewCount(): Long
}
