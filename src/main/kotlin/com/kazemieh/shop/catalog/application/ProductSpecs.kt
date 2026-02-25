package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.persistence.entity.*
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal

object ProductSpecs {

    fun active(): Specification<ProductEntity> =
        Specification { root, _, cb -> cb.isTrue(root.get("isActive")) }

    fun titleContains(q: String): Specification<ProductEntity> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("title")), "%${q.lowercase()}%")
        }

    fun categoryId(categoryId: Long): Specification<ProductEntity> =
        Specification { root, _, cb ->
            cb.equal(root.get<CategoryEntity>("category").get<Long>("id"), categoryId)
        }

    fun sizeId(sizeId: Long): Specification<ProductEntity> =
        Specification { root, query, cb ->
            query.distinct(true)
            val v = root.join<ProductEntity, ProductVariantEntity>("id") // will be replaced in service (see note)
            cb.conjunction()
        }

    fun variantFilters(
        sizeId: Long?,
        colorId: Long?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        inStock: Boolean?,
    ): Specification<ProductEntity> =
        Specification { root, query, cb ->
            query.distinct(true)

            // join with product_variants via subquery (بدون داشتن mapping داخل ProductEntity)
            val sub = query.subquery(Long::class.java)
            val pv = sub.from(ProductVariantEntity::class.java)
            sub.select(pv.get<ProductEntity>("product").get("id"))

            val preds = mutableListOf(
                cb.isTrue(pv.get("isActive"))
            )

            sizeId?.let { preds += cb.equal(pv.get<SizeEntity>("size").get<Long>("id"), it) }
            colorId?.let { preds += cb.equal(pv.get<ColorEntity>("color").get<Long>("id"), it) }
            minPrice?.let { preds += cb.greaterThanOrEqualTo(pv.get("price"), it) }
            maxPrice?.let { preds += cb.lessThanOrEqualTo(pv.get("price"), it) }

            // inStock: با subquery روی inventory (به‌صورت ساده)
            if (inStock == true) {
                val invSub = query.subquery(Long::class.java)
                val inv = invSub.from(com.kazemieh.shop.catalog.persistence.entity.InventoryEntity::class.java)
                invSub.select(inv.get("variantId"))
                invSub.where(
                    cb.equal(inv.get<Long>("variantId"), pv.get<Long>("id")),
                    cb.greaterThan(
                        cb.diff(inv.get<Int>("onHand"), inv.get<Int>("reserved")),
                        0
                    )
                )
                preds += cb.exists(invSub)
            }

            sub.where(*preds.toTypedArray())
            cb.in(root.get<Long>("id")).value(sub)
        }
}