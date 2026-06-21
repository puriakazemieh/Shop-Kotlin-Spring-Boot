package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductQuestionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductQuestionRepository : JpaRepository<ProductQuestionEntity, Long> {
    @Query("SELECT q FROM ProductQuestionEntity q WHERE (:productId IS NULL OR q.product.id = :productId) AND (:isNew IS NULL OR q.isNew = :isNew)")
    fun findAllByFilters(productId: Long?, isNew: Boolean?, pageable: Pageable): Page<ProductQuestionEntity>

    fun findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId: Long): List<ProductQuestionEntity>

    fun deleteAllByProductId(productId: Long)
}
