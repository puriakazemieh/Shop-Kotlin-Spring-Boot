package com.kazemieh.shop.catalog.api.mapper

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.persistence.entity.*

object AdminCatalogMapper {

    fun category(c: CategoryEntity) = AdminCategoryResponse(
        id = c.id,
        name = c.name,
        slug = c.slug,
        parentId = c.parent?.id,
        createdAt = c.createdAt
    )

    fun product(p: ProductEntity) = AdminProductResponse(
        id = p.id,
        title = p.title,
        slug = p.slug,
        categoryId = p.category?.id,
        isActive = p.isActive,
        createdAt = p.createdAt,
        updatedAt = p.updatedAt,
        basePrice = p.basePrice,
        description = p.description,
    )

    fun image(i: ProductImageEntity) = AdminProductImageResponse(
        id = i.id,
        url = i.url,
        sortOrder = i.sortOrder
    )

    fun inventory(inv: InventoryEntity) = AdminInventoryResponse(
        variantId = inv.variantId,
        onHand = inv.onHand,
        reserved = inv.reserved,
        available = (inv.onHand - inv.reserved).coerceAtLeast(0),
        version = inv.version,
        updatedAt = inv.updatedAt
    )

    fun variant(v: ProductVariantEntity, inv: InventoryEntity?) = AdminVariantResponse(
        id = v.id,
        productId = v.product!!.id,
        options = v.optionValues.associate { it.optionType.name to it.value },
        sku = v.sku,
        price = v.price,
        compareAtPrice = v.compareAtPrice,
        isActive = v.isActive,
        inventory = inv?.let(::inventory)
    )
}
