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

    @GetMapping("/sizes")
    fun sizes() = catalogService.sizes()

    @GetMapping("/colors")
    fun colors() = catalogService.colors()

    @GetMapping("/products")
    fun products(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) sizeId: Long?,
        @RequestParam(required = false) colorId: Long?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) inStock: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?, // newest | price_asc | price_desc
    ): PageResponse<ProductSummaryResponse> =
        catalogService.listProducts(q, categoryId, sizeId, colorId, minPrice, maxPrice, inStock, page, size, sort)

    @GetMapping("/products/{slug}")
    fun productDetail(@PathVariable slug: String): ProductDetailResponse =
        catalogService.productDetail(slug)
}