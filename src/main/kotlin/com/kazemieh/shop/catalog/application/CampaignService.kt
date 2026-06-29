package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.CampaignAdminResponse
import com.kazemieh.shop.catalog.api.dto.CampaignResponse
import com.kazemieh.shop.catalog.api.dto.CampaignUpsertRequest
import com.kazemieh.shop.catalog.api.dto.ProductSummaryResponse
import com.kazemieh.shop.catalog.persistence.CampaignRepository
import com.kazemieh.shop.catalog.persistence.FavoriteRepository
import com.kazemieh.shop.catalog.persistence.ProductImageRepository
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.ProductReviewRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.catalog.persistence.entity.CampaignEntity
import com.kazemieh.shop.catalog.persistence.entity.ProductEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.OffsetDateTime

@Service
class CampaignService(
    private val campaignRepository: CampaignRepository,
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val variantRepository: ProductVariantRepository,
    private val productReviewRepository: ProductReviewRepository,
    private val favoriteRepository: FavoriteRepository,
) {

    @Transactional(readOnly = true)
    fun getActiveCampaign(currentUserId: Long?): CampaignResponse? {
        val now = OffsetDateTime.now()
        val campaign = campaignRepository
            .findFirstByIsActiveTrueAndEndsAtAfterOrderByEndsAtAsc(now)
            ?: return null
        val products = campaign.products.filter { it.isActive }
        val remaining = Duration.between(now, campaign.endsAt).seconds.coerceAtLeast(0)
        return CampaignResponse(
            id = campaign.id,
            title = campaign.title,
            endsAt = campaign.endsAt.toString(),
            remainingSeconds = remaining,
            products = buildSummaries(products, currentUserId),
        )
    }

    @Transactional(readOnly = true)
    fun listAdmin(): List<CampaignAdminResponse> =
        campaignRepository.findAllByOrderByCreatedAtDesc().map { it.toAdminResponse() }

    @Transactional
    fun create(request: CampaignUpsertRequest): CampaignAdminResponse {
        val campaign = CampaignEntity(
            title = request.title,
            endsAt = OffsetDateTime.parse(request.endsAt),
            isActive = request.isActive,
            products = resolveProducts(request.productIds),
        )
        return campaignRepository.save(campaign).toAdminResponse()
    }

    @Transactional
    fun update(id: Long, request: CampaignUpsertRequest): CampaignAdminResponse {
        val campaign = campaignRepository.findById(id).orElseThrow {
            IllegalArgumentException("Campaign not found: $id")
        }
        campaign.title = request.title
        campaign.endsAt = OffsetDateTime.parse(request.endsAt)
        campaign.isActive = request.isActive
        campaign.products = resolveProducts(request.productIds)
        return campaignRepository.save(campaign).toAdminResponse()
    }

    @Transactional
    fun delete(id: Long) {
        campaignRepository.deleteById(id)
    }

    private fun resolveProducts(productIds: List<Long>): MutableList<ProductEntity> =
        if (productIds.isEmpty()) mutableListOf()
        else productRepository.findAllById(productIds).toMutableList()

    private fun CampaignEntity.toAdminResponse() = CampaignAdminResponse(
        id = id,
        title = title,
        endsAt = endsAt.toString(),
        isActive = isActive,
        productIds = products.map { it.id },
    )

    /** سرهم‌سازیِ ProductSummaryResponse برای مجموعه‌ای از محصولات (هم‌تراز با CatalogService). */
    private fun buildSummaries(products: List<ProductEntity>, currentUserId: Long?): List<ProductSummaryResponse> {
        if (products.isEmpty()) return emptyList()
        val productIds = products.map { it.id }

        val thumbnailByProductId = productImageRepository
            .findAllByProductIdInOrderBySortOrderAsc(productIds)
            .groupBy { it.product?.id ?: 0L }
            .mapValues { (_, list) -> list.firstOrNull()?.url }

        val aggByProductId = variantRepository.aggregateByProductIds(productIds)
            .associateBy { it.getProductId() }

        val ratingByProductId = productReviewRepository.aggregateRatingsByProductIds(productIds)
            .associateBy { it.getProductId() }

        val favoriteProductIds = if (currentUserId != null) {
            favoriteRepository.findAllByUserIdAndProductIdIn(currentUserId, productIds)
                .map { it.product.id }.toSet()
        } else emptySet()

        return products.map { p ->
            val agg = aggByProductId[p.id]
            val rating = ratingByProductId[p.id]
            ProductSummaryResponse(
                id = p.id,
                title = p.title,
                slug = p.slug,
                thumbnailUrl = thumbnailByProductId[p.id],
                minPrice = agg?.getMinPrice(),
                maxPrice = agg?.getMaxPrice(),
                minDiscountedPrice = agg?.getMinDiscountedPrice(),
                maxDiscountedPrice = agg?.getMaxDiscountedPrice(),
                inStock = agg?.getInStock() ?: false,
                categoryId = p.category?.id,
                categoryName = p.category?.name,
                isFavorite = favoriteProductIds.contains(p.id),
                averageRating = rating?.getAvgRating(),
                reviewCount = rating?.getReviewCount() ?: 0,
            )
        }
    }
}
