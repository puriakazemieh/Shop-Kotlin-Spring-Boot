package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductVariantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface ProductVariantRepository : JpaRepository<ProductVariantEntity, Long> {

    @Query(
        """
        select v from ProductVariantEntity v
        join fetch v.size
        join fetch v.color
        where v.product.id = :productId and v.isActive = true
        order by v.id asc
        """
    )
    fun findActiveWithOptions(@Param("productId") productId: Long): List<ProductVariantEntity>

    interface ProductAggregateRow {
        fun getProductId(): Long
        fun getMinPrice(): BigDecimal
        fun getMaxPrice(): BigDecimal
        fun getInStock(): Boolean
    }

    @Query(
        value = """
        select 
          pv.product_id as productId,
          min(pv.price) as minPrice,
          max(pv.price) as maxPrice,
          bool_or((coalesce(i.on_hand,0) - coalesce(i.reserved,0)) > 0) as inStock
        from product_variants pv
        left join inventory i on i.variant_id = pv.id
        where pv.is_active = true and pv.product_id in (:productIds)
        group by pv.product_id
        """,
        nativeQuery = true
    )
    fun aggregateByProductIds(@Param("productIds") productIds: List<Long>): List<ProductAggregateRow>

    fun existsBySku(sku: String): Boolean

    @Query(
        """
        select v from ProductVariantEntity v
        join fetch v.size
        join fetch v.color
        where v.product.id = :productId
        order by v.id asc
        """
    )
    fun findAllWithOptionsByProductId(@Param("productId") productId: Long): List<ProductVariantEntity>

    @Query(
        """
  select v from ProductVariantEntity v
  join fetch v.product p
  join fetch v.size s
  join fetch v.color c
  where v.id in :ids
  """
    )
    fun findWithAllOptionsByIds(@Param("ids") ids: List<Long>): List<ProductVariantEntity>
}