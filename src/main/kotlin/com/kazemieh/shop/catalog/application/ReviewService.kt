package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.ProductReviewRepository
import com.kazemieh.shop.catalog.persistence.entity.ProductReviewEntity
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.ApiException
import com.kazemieh.shop.shared.error.ErrorCodes
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ReviewService(
    private val reviewRepository: ProductReviewRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun listReviewsAdmin(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): PageResponse<AdminInteractionResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = reviewRepository.findAllByFilters(productId, isNew, pageable)

        return PageResponse(
            items = result.content.map { it.toAdminResponse() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getReviewsByProduct(productId: Long): List<ReviewResponse> {
        val reviews = reviewRepository.findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId)
        return reviews.map { it.toResponse() }
    }

    @Transactional
    fun createReview(userId: Long, request: CreateReviewRequest): ReviewResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow { ApiException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found", HttpStatus.NOT_FOUND) }

        val user = userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCodes.USER_NOT_FOUND, "User not found", HttpStatus.NOT_FOUND) }

        val parent = request.parentId?.let {
            reviewRepository.findById(it)
                .orElseThrow { ApiException(ErrorCodes.REVIEW_NOT_FOUND, "Parent review not found", HttpStatus.NOT_FOUND) }
        }

        val review = ProductReviewEntity(
            product = product,
            user = user,
            rating = if (parent == null) request.rating else null, // Rating only for top-level reviews
            comment = request.comment,
            parent = parent,
            isNew = true
        )

        return reviewRepository.save(review).toResponse()
    }

    @Transactional
    fun updateReview(userId: Long, reviewId: Long, request: UpdateReviewRequest): ReviewResponse {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ApiException(ErrorCodes.REVIEW_NOT_FOUND, "Review not found", HttpStatus.NOT_FOUND) }

        if (review.user.id != userId) {
            throw ApiException(ErrorCodes.ACCESS_DENIED, "You can only edit your own reviews", HttpStatus.FORBIDDEN)
        }

        review.rating = if (review.parent == null) request.rating else null
        review.comment = request.comment

        return reviewRepository.save(review).toResponse()
    }

    @Transactional
    fun deleteReview(userId: Long, reviewId: Long) {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ApiException(ErrorCodes.REVIEW_NOT_FOUND, "Review not found", HttpStatus.NOT_FOUND) }

        if (review.user.id != userId) {
            throw ApiException(ErrorCodes.ACCESS_DENIED, "You can only delete your own reviews", HttpStatus.FORBIDDEN)
        }

        reviewRepository.delete(review)
    }

    private fun ProductReviewEntity.toResponse(): ReviewResponse {
        return ReviewResponse(
            id = this.id,
            userId = this.user.id,
            userName = "${this.user.firstName ?: ""} ${this.user.lastName ?: ""}".trim(),
            rating = this.rating,
            comment = this.comment,
            replies = this.replies.map { it.toResponse() },
            createdAt = this.createdAt ?: OffsetDateTime.now()
        )
    }

    private fun ProductReviewEntity.toAdminResponse(): AdminInteractionResponse {
        return AdminInteractionResponse(
            id = this.id,
            productId = this.product.id,
            productTitle = this.product.title,
            userId = this.user.id,
            userName = "${this.user.firstName ?: ""} ${this.user.lastName ?: ""}".trim(),
            content = this.comment,
            rating = this.rating,
            isNew = this.isNew,
            createdAt = this.createdAt ?: OffsetDateTime.now()
        )
    }
}
