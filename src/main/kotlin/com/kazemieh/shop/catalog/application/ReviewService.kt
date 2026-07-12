package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.ProductReviewHelpfulRepository
import com.kazemieh.shop.catalog.persistence.ProductReviewRepository
import com.kazemieh.shop.catalog.persistence.entity.ProductReviewEntity
import com.kazemieh.shop.catalog.persistence.entity.ProductReviewHelpfulEntity
import com.kazemieh.shop.identity.domain.UserRole
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.order.persistence.OrderRepository
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
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val helpfulRepository: ProductReviewHelpfulRepository
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
    fun getReviewsByProduct(productId: Long, currentUserId: Long?): List<ReviewResponse> {
        val reviews = reviewRepository.findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId)
        val helpfulIds: Set<Long> = currentUserId
            ?.let { helpfulRepository.findReviewIdsByUserAndProduct(it, productId).toSet() }
            ?: emptySet()
        return reviews.map { it.toResponse(helpfulIds) }
    }

    /**
     * toggle رأیِ «مفید بود» برای کاربرِ جاری روی یک نظر.
     * اگر قبلاً رأی داده باشد، رأی برداشته و شمارنده کم می‌شود؛ در غیر این صورت افزوده می‌شود.
     */
    @Transactional
    fun toggleHelpful(userId: Long, reviewId: Long): ReviewResponse {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ApiException(ErrorCodes.REVIEW_NOT_FOUND, "Review not found", HttpStatus.NOT_FOUND) }

        val existing = helpfulRepository.findByReviewIdAndUserId(reviewId, userId)
        val helpfulByMe: Boolean
        if (existing != null) {
            helpfulRepository.delete(existing)
            review.helpfulCount = (review.helpfulCount - 1).coerceAtLeast(0)
            helpfulByMe = false
        } else {
            helpfulRepository.save(ProductReviewHelpfulEntity(review = review, userId = userId))
            review.helpfulCount += 1
            helpfulByMe = true
        }
        val saved = reviewRepository.save(review)
        return saved.toResponse(if (helpfulByMe) setOf(saved.id) else emptySet())
    }

    @Transactional(readOnly = true)
    fun getReviewsByProduct(productId: Long): List<ReviewResponse> {
        val reviews = reviewRepository.findAllByProductIdAndParentIsNullOrderByCreatedAtDesc(productId)
        val purchasers = orderRepository.findPurchaserIdsByProduct(productId).toSet()
        return reviews.map { it.toResponse(purchasers) }
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

        val purchasers = orderRepository.findPurchaserIdsByProduct(product.id).toSet()
        return reviewRepository.save(review).toResponse(purchasers)
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

        val purchasers = orderRepository.findPurchaserIdsByProduct(review.product.id).toSet()
        return reviewRepository.save(review).toResponse(purchasers)
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

    private fun ProductReviewEntity.toResponse(purchasers: Set<Long> = emptySet()): ReviewResponse {
        val isSupport = this.user.role == UserRole.ADMIN
        val displayName = if (isSupport) "پشتیبانی کارمیلا"
            else "${this.user.firstName ?: ""} ${this.user.lastName ?: ""}".trim()
        return ReviewResponse(
            id = this.id,
            userId = this.user.id,
            userName = displayName,
            rating = this.rating,
            comment = this.comment,
            replies = this.replies.map { it.toResponse(purchasers) },
            createdAt = this.createdAt ?: OffsetDateTime.now(),
            isSupport = isSupport,
            verifiedPurchase = !isSupport && purchasers.contains(this.user.id)
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
