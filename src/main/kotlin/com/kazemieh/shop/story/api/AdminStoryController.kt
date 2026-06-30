package com.kazemieh.shop.story.api

import com.kazemieh.shop.story.api.dto.AdminCreateStoryRequest
import com.kazemieh.shop.story.api.dto.AdminUpdateStoryRequest
import com.kazemieh.shop.story.application.StoryService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/stories")
@PreAuthorize("hasRole('ADMIN')")
class AdminStoryController(
    private val storyService: StoryService
) {

    @GetMapping
    fun list() = storyService.getAllStoriesForAdmin()

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun create(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(value = "productId", required = false) productId: Long?,
        @RequestParam(value = "linkType", defaultValue = "NONE") linkType: String,
        @RequestParam(value = "categoryId", required = false) categoryId: Long?,
        @RequestParam(value = "blogSlug", required = false) blogSlug: String?,
        @RequestParam(value = "title", required = false) title: String?,
        @RequestParam(value = "durationHours", defaultValue = "24") durationHours: Long
    ) = storyService.createStory(
        file,
        AdminCreateStoryRequest(productId, linkType, categoryId, blogSlug, title, durationHours)
    )

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody req: AdminUpdateStoryRequest
    ) = storyService.updateStory(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        storyService.deleteStory(id)
    }
}
