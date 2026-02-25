package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminAddImageRequest
import com.kazemieh.shop.catalog.api.dto.AdminReorderImagesRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/products/{productId}/images")
@PreAuthorize("hasRole('ADMIN')")
class AdminProductImageController(
    private val adminCatalogService: AdminCatalogService
) {

    @PostMapping
    fun add(
        @PathVariable productId: Long,
        @Valid @RequestBody req: AdminAddImageRequest
    ) = adminCatalogService.addImage(productId, req)

    @PatchMapping("/reorder")
    fun reorder(
        @PathVariable productId: Long,
        @Valid @RequestBody req: AdminReorderImagesRequest
    ) = adminCatalogService.reorderImages(productId, req)

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable productId: Long,
        @PathVariable imageId: Long
    ) {
        adminCatalogService.deleteImage(productId, imageId)
    }
}