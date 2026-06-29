package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.BannerResponse
import com.kazemieh.shop.catalog.api.dto.BannerUpsertRequest
import com.kazemieh.shop.catalog.persistence.BannerRepository
import com.kazemieh.shop.catalog.persistence.entity.BannerEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BannerService(
    private val bannerRepository: BannerRepository
) {

    @Transactional(readOnly = true)
    fun listActive(): List<BannerResponse> =
        bannerRepository.findAllByIsActiveTrueOrderBySortOrderAsc().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun listAll(): List<BannerResponse> =
        bannerRepository.findAllByOrderBySortOrderAsc().map { it.toResponse() }

    @Transactional
    fun create(request: BannerUpsertRequest): BannerResponse =
        bannerRepository.save(
            BannerEntity(
                title = request.title,
                subtitle = request.subtitle,
                imageUrl = request.imageUrl,
                categoryId = request.categoryId,
                sortOrder = request.sortOrder,
                isActive = request.isActive,
            )
        ).toResponse()

    @Transactional
    fun update(id: Long, request: BannerUpsertRequest): BannerResponse {
        val banner = bannerRepository.findById(id).orElseThrow {
            IllegalArgumentException("Banner not found: $id")
        }
        banner.title = request.title
        banner.subtitle = request.subtitle
        banner.imageUrl = request.imageUrl
        banner.categoryId = request.categoryId
        banner.sortOrder = request.sortOrder
        banner.isActive = request.isActive
        return bannerRepository.save(banner).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        bannerRepository.deleteById(id)
    }

    private fun BannerEntity.toResponse() = BannerResponse(
        id = id,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        categoryId = categoryId,
        sortOrder = sortOrder,
        isActive = isActive,
    )
}
