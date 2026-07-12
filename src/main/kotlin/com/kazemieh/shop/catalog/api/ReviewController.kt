package com.kazemieh.shop.catalog.api

import com.kazemieh.shop.catalog.api.dto.CreateReviewRequest
import com.kazemieh.shop.catalog.api.dto.ReviewResponse
import com.kazemieh.shop.catalog.api.dto.UpdateReviewRequest
import com.kazemieh.shop.catalog.application.ReviewService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    @GetMapping("/product/{productId}")
    fun getReviews(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable productId: Long
    ): List<ReviewResponse> {
        return reviewService.getReviewsByProduct(productId, principal?.id)
    }

    @PostMapping("/{reviewId}/helpful")
    fun toggleHelpful(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable reviewId: Long
    ): ReviewResponse {
        return reviewService.toggleHelpful(principal.id, reviewId)
    }

    @PostMapping
    fun createReview(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateReviewRequest
    ): ReviewResponse {
        return reviewService.createReview(principal.id, request)
    }

    @PutMapping("/{reviewId}")
    fun updateReview(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable reviewId: Long,
        @RequestBody request: UpdateReviewRequest
    ): ReviewResponse {
        return reviewService.updateReview(principal.id, reviewId, request)
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable reviewId: Long
    ) {
        reviewService.deleteReview(principal.id, reviewId)
    }
}
