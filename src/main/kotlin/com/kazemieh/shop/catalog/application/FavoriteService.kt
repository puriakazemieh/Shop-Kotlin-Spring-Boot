package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.PageResponse
import com.kazemieh.shop.catalog.api.dto.ProductSummaryResponse
import com.kazemieh.shop.catalog.application.exception.ProductNotFoundException
import com.kazemieh.shop.catalog.persistence.FavoriteRepository
import com.kazemieh.shop.catalog.persistence.ProductImageRepository
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.ProductReviewRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.catalog.persistence.entity.FavoriteEntity
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val productImageRepository: ProductImageRepository,
    private val variantRepository: ProductVariantRepository,
    private val productReviewRepository: ProductReviewRepository,
) {

    @Transactional
    fun addToFavorites(userId: Long, productId: Long) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) return

        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val product = productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        favoriteRepository.save(FavoriteEntity(user = user, product = product))
    }

    @Transactional
    fun removeFromFavorites(userId: Long, productId: Long) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId)
    }

    @Transactional(readOnly = true)
    fun getFavorites(userId: Long, page: Int, size: Int): PageResponse<ProductSummaryResponse> {
        val pageable = PageRequest.of(page, size)
        val favoritePage = favoriteRepository.findAllByUserId(userId, pageable)
        
        val products = favoritePage.content.map { it.product }
        val productIds = products.map { it.id }

        val images = if (productIds.isNotEmpty())
            productImageRepository.findAllByProductIdInOrderBySortOrderAsc(productIds)
        else emptyList()

        val thumbnailByProductId = images
            .groupBy { it.product?.id ?: 0L }
            .mapValues { (_, list) -> list.firstOrNull()?.url }

        val aggByProductId = if (productIds.isNotEmpty())
            variantRepository.aggregateByProductIds(productIds).associateBy { it.getProductId() }
        else emptyMap()

        val ratingByProductId = if (productIds.isNotEmpty())
            productReviewRepository.aggregateRatingsByProductIds(productIds).associateBy { it.getProductId() }
        else emptyMap()

        val items = products.map { p ->
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
                isFavorite = true,
                averageRating = rating?.getAvgRating(),
                reviewCount = rating?.getReviewCount() ?: 0,
            )
        }

        return PageResponse(
            items = items,
            page = favoritePage.number,
            size = favoritePage.size,
            totalElements = favoritePage.totalElements,
            totalPages = favoritePage.totalPages
        )
    }
}
