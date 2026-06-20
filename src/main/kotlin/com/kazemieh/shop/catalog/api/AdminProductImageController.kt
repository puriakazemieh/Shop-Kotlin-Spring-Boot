package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminAddImageRequest
import com.kazemieh.shop.catalog.api.dto.AdminProductImageResponse
import com.kazemieh.shop.catalog.api.dto.AdminReorderImagesRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import com.kazemieh.shop.catalog.application.FileStorageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.MediaType


@RestController
@RequestMapping("/api/admin/products/{productId}/images")
@PreAuthorize("hasRole('ADMIN')")
class AdminProductImageController(
    private val adminCatalogService: AdminCatalogService,
    private val fileStorageService: FileStorageService
) {


    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun add(
        @PathVariable productId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(value = "sortOrder", required = false) sortOrder: Int?
    ): AdminProductImageResponse {
        val contentType = file.contentType
        if (contentType == null || !contentType.startsWith("image/")) {
            throw com.kazemieh.shop.catalog.application.exception.BadRequestException(
                "فایل ارسالی باید تصویر باشد",
                "INVALID_IMAGE_TYPE"
            )
        }
        val imageUrl = fileStorageService.saveFile(file)
        val req = AdminAddImageRequest(url = imageUrl, sortOrder = sortOrder)
        return adminCatalogService.addImage(productId, req)
    }

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