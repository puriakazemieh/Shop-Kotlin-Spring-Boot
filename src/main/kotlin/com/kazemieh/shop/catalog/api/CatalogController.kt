package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.PageResponse
import com.kazemieh.shop.catalog.api.dto.ProductDetailResponse
import com.kazemieh.shop.catalog.api.dto.ProductSummaryResponse
import com.kazemieh.shop.catalog.application.CatalogService
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api")
class CatalogController(
    private val catalogService: CatalogService
) {

    @GetMapping("/categories")
    fun categories() = catalogService.categoriesTree()

    @GetMapping("/products")
    fun products(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) categorySlug: String?,
        @RequestParam(required = false) options: Map<String, String>?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) inStock: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?, // newest | price_asc | price_desc
    ): PageResponse<ProductSummaryResponse> =
        catalogService.listProducts(
            q = q,
            categoryId = categoryId,
            options = options,
            minPrice = minPrice,
            maxPrice = maxPrice,
            inStock = inStock,
            page = page,
            size = size,
            categorySlug = sort,
            sort = categorySlug
        )

    @GetMapping("/products/{slug}")
    fun productDetail(@PathVariable slug: String): ProductDetailResponse =
        catalogService.productDetail(slug)
}
