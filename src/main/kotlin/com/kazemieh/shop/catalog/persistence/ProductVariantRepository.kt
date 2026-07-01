package com.kazemieh.shop.catalog.persistence

import com.kazemieh.shop.catalog.persistence.entity.ProductVariantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.Optional

interface ProductVariantRepository : JpaRepository<ProductVariantEntity, Long> {

    @Query(
        """
        select v from ProductVariantEntity v
        left join fetch v.optionValues ov
        left join fetch ov.optionType
        where v.product.id = :productId and v.isActive = true
        order by v.id asc
        """
    )
    fun findActiveWithOptions(@Param("productId") productId: Long): List<ProductVariantEntity>

    interface ProductAggregateRow {
        fun getProductId(): Long
        fun getMinPrice(): BigDecimal
        fun getMaxPrice(): BigDecimal
        fun getMinDiscountedPrice(): BigDecimal?
        fun getMaxDiscountedPrice(): BigDecimal?
        fun getInStock(): Boolean
    }

    interface ProductStockRow {
        fun getProductId(): Long
        fun getStock(): Long
    }

    /** مجموعِ موجودیِ در دسترس (on_hand - reserved) روی همه‌ی واریانت‌های فعالِ هر محصول. */
    @Query(
        value = """
        select
          pv.product_id as productId,
          coalesce(sum(coalesce(i.on_hand,0) - coalesce(i.reserved,0)), 0) as stock
        from product_variants pv
        left join inventory i on i.variant_id = pv.id
        where pv.is_active = true and pv.product_id in (:productIds)
        group by pv.product_id
        """,
        nativeQuery = true
    )
    fun stockByProductIds(@Param("productIds") productIds: List<Long>): List<ProductStockRow>

    @Query(
        value = """
        select 
          pv.product_id as productId,
          min(pv.price) as minPrice,
          max(pv.price) as maxPrice,
          min(coalesce(pv.discounted_price, p.discounted_price)) as minDiscountedPrice,
          max(coalesce(pv.discounted_price, p.discounted_price)) as maxDiscountedPrice,
          bool_or((coalesce(i.on_hand,0) - coalesce(i.reserved,0)) > 0) as inStock
        from product_variants pv
        left join inventory i on i.variant_id = pv.id
        join products p on p.id = pv.product_id
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
        left join fetch v.optionValues ov
        left join fetch ov.optionType
        where v.product.id = :productId
        order by v.id asc
        """
    )
    fun findAllByProductId(@Param("productId") productId: Long): List<ProductVariantEntity>

    @Query(
        """
        select v from ProductVariantEntity v
        left join fetch v.product p
        left join fetch v.optionValues ov
        left join fetch ov.optionType
        where v.id in :ids
        """
    )
    fun findWithAllOptionsByIds(@Param("ids") ids: List<Long>): List<ProductVariantEntity>

    @Query(
        """
        select v from ProductVariantEntity v
        left join fetch v.product p
        left join fetch v.optionValues ov
        left join fetch ov.optionType
        where v.id = :id
        """
    )
    fun findWithAllOptionsById(@Param("id") id: Long): Optional<ProductVariantEntity>

    @Query(
        value = """
        select 
          pv.id as variantId,
          pv.price as price,
          p.title as title,
          pv.is_active as isActive
        from product_variants pv
        join products p on p.id = pv.product_id
        where pv.id in (:ids)
        """,
        nativeQuery = true
    )
    fun findSnapshots(@Param("ids") ids: List<Long>): List<VariantSnapshotProjection>
}