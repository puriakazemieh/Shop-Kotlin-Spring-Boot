package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    fun findBySlugAndIsActiveTrue(slug: String): ProductEntity?
    fun findBySlug(slug: String): ProductEntity?
    fun existsBySlug(slug: String): Boolean

    /** شناسه‌ی محصولاتی که در سفارش‌هایِ گذشته با این محصول هم‌رخداد داشته‌اند، به‌ترتیبِ فراوانی. */
    @Query(
        value = """
            SELECT pv2.product_id
            FROM order_items oi1
            JOIN product_variants pv1 ON pv1.id = oi1.variant_id AND pv1.product_id = :productId
            JOIN order_items oi2 ON oi2.order_id = oi1.order_id AND oi2.variant_id <> oi1.variant_id
            JOIN product_variants pv2 ON pv2.id = oi2.variant_id AND pv2.product_id <> :productId
            GROUP BY pv2.product_id
            ORDER BY COUNT(*) DESC
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findFrequentlyBoughtTogetherIds(@Param("productId") productId: Long, @Param("limit") limit: Int): List<Long>
}