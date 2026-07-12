package com.kazemieh.shop.catalog.api.dto

/**
 * باندل/پکیجِ محصول. خودِ باندل یک محصولِ واقعیِ قابلِ‌خرید است (product) با قیمتِ مستقلِ خودش؛
 * members فقط برای نمایشِ «این باندل شامل چه چیزهایی است» است (خریدِ مستقیم از طریقِ همان محصول).
 */
data class BundleSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val product: ProductSummaryResponse,
    val memberCount: Int
)

data class BundleDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val product: ProductSummaryResponse,
    val members: List<ProductSummaryResponse>
)

data class AdminBundleResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val productId: Long,
    val memberProductIds: List<Long>,
    val isActive: Boolean
)

data class AdminCreateBundleRequest(
    val title: String,
    val slug: String,
    val description: String? = null,
    val productId: Long,
    val memberProductIds: List<Long> = emptyList(),
    val isActive: Boolean = true
)

data class AdminUpdateBundleRequest(
    val title: String? = null,
    val description: String? = null,
    val memberProductIds: List<Long>? = null,
    val isActive: Boolean? = null
)
