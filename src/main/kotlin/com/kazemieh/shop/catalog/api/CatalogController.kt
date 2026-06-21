package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.PageResponse
import com.kazemieh.shop.catalog.api.dto.ProductDetailResponse
import com.kazemieh.shop.catalog.api.dto.ProductSummaryResponse
import com.kazemieh.shop.catalog.application.CatalogService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
        @AuthenticationPrincipal principal: UserPrincipal?,
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
            categorySlug = categorySlug,
            sort = sort,
            currentUserId = principal?.id
        )

    @GetMapping("/products/{slug}")
    fun productDetail(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: UserPrincipal?,
    ): ProductDetailResponse =
        catalogService.productDetail(slug, principal?.id)
}
