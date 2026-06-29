package com.kazemieh.shop.catalog.api.dto

/** پاسخِ عمومیِ کمپینِ فعال برای صفحه‌ی اصلی. */
data class CampaignResponse(
    val id: Long,
    val title: String,
    val endsAt: String,
    val remainingSeconds: Long,
    val products: List<ProductSummaryResponse>,
)

/** نمای ادمینِ یک کمپین. */
data class CampaignAdminResponse(
    val id: Long,
    val title: String,
    val endsAt: String,
    val isActive: Boolean,
    val productIds: List<Long>,
)

/** ورودیِ ساخت/ویرایشِ کمپین (ادمین). */
data class CampaignUpsertRequest(
    val title: String,
    val endsAt: String,
    val productIds: List<Long> = emptyList(),
    val isActive: Boolean = true,
)
