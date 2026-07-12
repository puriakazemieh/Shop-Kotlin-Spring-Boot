package com.kazemieh.shop.order.api.dto

import com.kazemieh.shop.order.persistence.entity.ReturnRequestStatus
import com.kazemieh.shop.order.persistence.entity.ReturnRequestType
import java.time.OffsetDateTime

data class CreateReturnRequestRequest(
    val orderItemId: Long,
    val type: ReturnRequestType,
    val reason: String
)

data class ReturnRequestResponse(
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val itemTitle: String,
    val type: ReturnRequestType,
    val reason: String,
    val status: ReturnRequestStatus,
    val adminNote: String?,
    val createdAt: OffsetDateTime?,
    val resolvedAt: OffsetDateTime?
)

data class AdminReturnRequestResponse(
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val itemTitle: String,
    val userId: Long,
    val userName: String?,
    val type: ReturnRequestType,
    val reason: String,
    val status: ReturnRequestStatus,
    val adminNote: String?,
    val createdAt: OffsetDateTime?,
    val resolvedAt: OffsetDateTime?
)

data class AdminUpdateReturnRequestRequest(
    val status: ReturnRequestStatus,
    val adminNote: String? = null
)
