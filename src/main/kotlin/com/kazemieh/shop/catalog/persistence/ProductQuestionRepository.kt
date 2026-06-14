package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductQuestionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductQuestionRepository : JpaRepository<ProductQuestionEntity, Long> {
    fun findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId: Long): List<ProductQuestionEntity>
}
