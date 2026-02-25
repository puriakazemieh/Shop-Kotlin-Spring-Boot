package com.kazemieh.shop.catalog.api.mapper

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.persistence.entity.*

object CatalogMapper {

    fun toSize(s: SizeEntity) = SizeResponse(s.id, s.name, s.sortOrder)
    fun toColor(c: ColorEntity) = ColorResponse(c.id, c.name, c.hex)

    fun toImage(i: ProductImageEntity) = ProductImageResponse(i.url, i.sortOrder)

    fun toVariant(v: ProductVariantEntity, availableQty: Int) = VariantResponse(
        id = v.id,
        sku = v.sku,
        price = v.price,
        compareAtPrice = v.compareAtPrice,
        size = toSize(v.size!!),
        color = toColor(v.color!!),
        availableQty = availableQty,
        isActive = v.isActive
    )
}