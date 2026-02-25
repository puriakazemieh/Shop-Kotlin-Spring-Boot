package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProductRepository : JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    fun findBySlugAndIsActiveTrue(slug: String): ProductEntity?
}