package com.kazemieh.shop.academy.courserequest

data class CourseRequestResponse(
    val id: Long,
    val title: String,
    val description: String? = null,
    val requesterName: String? = null,
    val likeCount: Int,
    /** آیا کاربرِ فعلی این درخواست را لایک کرده است؟ */
    val liked: Boolean = false,
    val fulfilled: Boolean = false,
    val createdAt: String? = null
)

data class CreateCourseRequestRequest(
    val title: String,
    val description: String? = null
)

data class ToggleLikeResponse(
    val liked: Boolean,
    val likeCount: Int
)
