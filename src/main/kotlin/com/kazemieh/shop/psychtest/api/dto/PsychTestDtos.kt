package com.kazemieh.shop.psychtest.api.dto

import java.math.BigDecimal

// ---------- Public catalog ----------
data class PsychTestSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val resultMode: String,
    val questionCount: Int,
    val owned: Boolean = false,
    val productId: Long? = null
)

data class TestOptionResponse(
    val text: String,
    /** فقط برای ادمین پر می‌شود؛ در endpointِ عمومی حذف می‌شود. */
    val score: Int? = null
)

data class TestQuestionResponse(
    val index: Int,
    val text: String,
    val options: List<TestOptionResponse>
)

data class ScoreRangeResponse(
    val minScore: Int,
    val maxScore: Int,
    val interpretation: String
)

data class PsychTestDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val resultMode: String,
    val questions: List<TestQuestionResponse>,
    val owned: Boolean,
    val productId: Long? = null
)

// ---------- My tests ----------
data class UserPsychTestResponse(
    val id: Long,
    val testId: Long,
    val testTitle: String,
    val status: String,
    val resultMode: String,
    val totalScore: Int?,
    val interpretation: String?,
    val completedAt: String?
)

data class SubmitTestRequest(
    /** questionIndex -> selectedOptionIndex */
    val answers: Map<Int, Int> = emptyMap()
)

// ---------- Admin ----------
data class AdminCreatePsychTestRequest(
    val title: String,
    val slug: String,
    val description: String? = null,
    val price: BigDecimal = BigDecimal.ZERO,
    val discountedPrice: BigDecimal? = null,
    val productId: Long? = null,
    val resultMode: String = "AUTO",
    val isPublished: Boolean = true,
    val questions: List<TestQuestionResponse> = emptyList(),
    val ranges: List<ScoreRangeResponse> = emptyList()
)

data class AdminUpdatePsychTestRequest(
    val title: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val discountedPrice: BigDecimal? = null,
    val resultMode: String? = null,
    val isPublished: Boolean? = null,
    val questions: List<TestQuestionResponse>? = null,
    val ranges: List<ScoreRangeResponse>? = null
)

data class AdminInterpretRequest(
    val interpretation: String
)
