package com.kazemieh.shop.catalog.api.mapper

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.persistence.entity.*

object CatalogMapper {

    fun toImage(i: ProductImageEntity) = ProductImageResponse(i.url, i.sortOrder)

    fun toVariant(v: ProductVariantEntity, availableQty: Int) = VariantResponse(
        id = v.id,
        sku = v.sku,
        price = v.price,
        compareAtPrice = v.compareAtPrice,
        options = v.optionValues.associate { it.optionType.name to it.value },
        availableQty = availableQty,
        isActive = v.isActive
    )
}
