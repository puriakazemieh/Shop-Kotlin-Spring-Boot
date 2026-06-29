package com.kazemieh.shop.catalog.api.dto

/** بنرِ تبلیغاتی (عمومی + ادمین از یک ساختار استفاده می‌کنند). */
data class BannerResponse(
    val id: Long,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val categoryId: Long?,
    val sortOrder: Int,
    val isActive: Boolean,
)

data class BannerUpsertRequest(
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val categoryId: Long? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)
