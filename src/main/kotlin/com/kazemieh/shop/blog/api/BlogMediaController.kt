package com.kazemieh.shop.blog.api

import com.kazemieh.shop.blog.api.dto.MediaUploadResponse
import com.kazemieh.shop.catalog.application.FileStorageService
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/blogs/media")
@PreAuthorize("hasRole('ADMIN')")
class BlogMediaController(
    private val fileStorageService: FileStorageService
) {

    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadMedia(@RequestParam("file") file: MultipartFile): MediaUploadResponse {
        val url = fileStorageService.saveFile(file)
        return MediaUploadResponse(url)
    }
}
