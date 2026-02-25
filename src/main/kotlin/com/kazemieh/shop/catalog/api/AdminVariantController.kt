package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminCreateVariantRequest
import com.kazemieh.shop.catalog.api.dto.AdminInventoryAdjustRequest
import com.kazemieh.shop.catalog.api.dto.AdminInventorySetRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateVariantRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@PreAuthorize("hasRole('ADMIN')")
class AdminVariantController(
    private val adminCatalogService: AdminCatalogService
) {

    @PostMapping("/api/admin/products/{productId}/variants")
    fun createVariant(
        @PathVariable productId: Long,
        @Valid @RequestBody req: AdminCreateVariantRequest
    ) = adminCatalogService.createVariant(productId, req)

    @PatchMapping("/api/admin/variants/{variantId}")
    fun updateVariant(
        @PathVariable variantId: Long,
        @Valid @RequestBody req: AdminUpdateVariantRequest
    ) = adminCatalogService.updateVariant(variantId, req)

    @GetMapping("/api/admin/variants/{variantId}/inventory")
    fun getInventory(@PathVariable variantId: Long) =
        adminCatalogService.getInventory(variantId)

    @PutMapping("/api/admin/variants/{variantId}/inventory")
    fun setInventory(
        @PathVariable variantId: Long,
        @Valid @RequestBody req: AdminInventorySetRequest
    ) = adminCatalogService.setInventory(variantId, req)

    @PatchMapping("/api/admin/variants/{variantId}/inventory/adjust")
    fun adjustInventory(
        @PathVariable variantId: Long,
        @Valid @RequestBody req: AdminInventoryAdjustRequest
    ) = adminCatalogService.adjustInventory(variantId, req)
}