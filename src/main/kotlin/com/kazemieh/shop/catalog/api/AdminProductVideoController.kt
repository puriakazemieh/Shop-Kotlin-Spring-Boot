package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.AdminAddVideoRequest
import com.kazemieh.shop.catalog.api.dto.AdminProductVideoResponse
import com.kazemieh.shop.catalog.api.dto.AdminReorderVideosRequest
import com.kazemieh.shop.catalog.application.AdminCatalogService
import com.kazemieh.shop.catalog.application.FileStorageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/products/{productId}/videos")
@PreAuthorize("hasRole('ADMIN')")
class AdminProductVideoController(
    private val adminCatalogService: AdminCatalogService,
    private val fileStorageService: FileStorageService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun add(
        @PathVariable productId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(value = "sortOrder", required = false) sortOrder: Int?
    ): AdminProductVideoResponse {
        val videoUrl = fileStorageService.saveFile(file)
        val req = AdminAddVideoRequest(url = videoUrl, sortOrder = sortOrder)
        return adminCatalogService.addVideo(productId, req)
    }

    @PatchMapping("/reorder")
    fun reorder(
        @PathVariable productId: Long,
        @Valid @RequestBody req: AdminReorderVideosRequest
    ) = adminCatalogService.reorderVideos(productId, req)

    @DeleteMapping("/{videoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable productId: Long,
        @PathVariable videoId: Long
    ) {
        adminCatalogService.deleteVideo(productId, videoId)
    }
}
