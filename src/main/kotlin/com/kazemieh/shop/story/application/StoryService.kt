package com.kazemieh.shop.story.application

import com.kazemieh.shop.catalog.application.FileStorageService
import com.kazemieh.shop.story.api.dto.AdminCreateStoryRequest
import com.kazemieh.shop.story.api.dto.AdminUpdateStoryRequest
import com.kazemieh.shop.story.api.dto.StoryResponse
import com.kazemieh.shop.story.persistence.StoryRepository
import com.kazemieh.shop.story.persistence.entity.StoryEntity
import com.kazemieh.shop.story.persistence.entity.StoryMediaType
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

@Service
class StoryService(
    private val storyRepository: StoryRepository,
    private val fileStorageService: FileStorageService
) {

    @Transactional(readOnly = true)
    fun getActiveStories(): List<StoryResponse> {
        val now = OffsetDateTime.now()
        return storyRepository.findAllByIsActiveTrueAndExpiresAtAfterOrderByCreatedAtDesc(now)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getAllStoriesForAdmin(): List<StoryResponse> {
        return storyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
            .map { it.toResponse() }
    }

    @Transactional
    fun createStory(file: MultipartFile, req: AdminCreateStoryRequest): StoryResponse {
        val mediaUrl = fileStorageService.saveFile(file)
        val mediaType = if (file.contentType?.startsWith("video") == true) {
            StoryMediaType.VIDEO
        } else {
            StoryMediaType.IMAGE
        }

        val story = StoryEntity(
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            productId = req.productId,
            title = req.title,
            expiresAt = OffsetDateTime.now().plusHours(req.durationHours)
        )

        return storyRepository.save(story).toResponse()
    }

    @Transactional
    fun updateStory(id: Long, req: AdminUpdateStoryRequest): StoryResponse {
        val story = storyRepository.findById(id).orElseThrow { RuntimeException("Story not found") }
        
        req.productId?.let { story.productId = it }
        req.title?.let { story.title = it }
        req.isActive?.let { story.isActive = it }

        return storyRepository.save(story).toResponse()
    }

    @Transactional
    fun deleteStory(id: Long) {
        storyRepository.deleteById(id)
    }

    private fun StoryEntity.toResponse() = StoryResponse(
        id = id,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        productId = productId,
        title = title,
        createdAt = createdAt
    )
}
