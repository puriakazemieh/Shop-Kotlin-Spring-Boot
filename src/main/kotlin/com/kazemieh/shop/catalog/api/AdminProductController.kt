package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminCreateProductRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateProductRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
class AdminProductController(
    private val adminCatalogService: AdminCatalogService
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "true") includeInactive: Boolean
    ) = adminCatalogService.listProducts(page, size, includeInactive)

    @PostMapping
    fun create(@Valid @RequestBody req: AdminCreateProductRequest) =
        adminCatalogService.createProduct(req)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long) =
        adminCatalogService.productDetail(id)

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody req: AdminUpdateProductRequest) =
        adminCatalogService.updateProduct(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteHard(@PathVariable id: Long) {
        adminCatalogService.deleteProductHard(id)
    }
}