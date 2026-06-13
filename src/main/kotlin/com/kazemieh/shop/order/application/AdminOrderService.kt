package com.kazemieh.shop.order.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.catalog.api.dto.PageResponse
import com.kazemieh.shop.order.api.dto.AdminOrderDetailResponse
import com.kazemieh.shop.order.api.dto.AdminOrderItemResponse
import com.kazemieh.shop.order.api.dto.AdminOrderSummaryResponse
import com.kazemieh.shop.order.api.dto.AdminUpdateOrderStatusRequest
import com.kazemieh.shop.order.application.exception.OrderNotFoundException
import com.kazemieh.shop.order.persistence.AdminOrderQueryRepository
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminOrderService(
    private val adminOrderQueryRepository: AdminOrderQueryRepository,
    private val orderService: OrderService, // reuse updateStatus logic (inventory handling)
    private val objectMapper: ObjectMapper,
) {

    @Transactional(readOnly = true)
    fun list(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): PageResponse<AdminOrderSummaryResponse> {

        val statusEnum = status?.trim()?.takeIf { it.isNotEmpty() }?.let { OrderStatus.valueOf(it.uppercase()) }

        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.by("createdAt").descending()
        )

        val data = adminOrderQueryRepository.search(statusEnum, userId, pageable)

        val items = data.content.map { o ->
            AdminOrderSummaryResponse(
                id = o.id,
                userId = o.user!!.id,
                identity = o.user!!.email ?: o.user!!.phone ?: "",
                status = o.status.name,
                totalPrice = o.totalPrice,
                createdAt = o.createdAt
            )
        }

        return PageResponse(
            items = items,
            page = data.number,
            size = data.size,
            totalElements = data.totalElements,
            totalPages = data.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun detail(orderId: Long): AdminOrderDetailResponse {
        val o = adminOrderQueryRepository.findDetail(orderId) ?: throw OrderNotFoundException(orderId)

        val addrAny: Any = objectMapper.convertValue(o.addressSnapshot, Any::class.java)

        return AdminOrderDetailResponse(
            id = o.id,
            userId = o.user!!.id,
            identity = o.user!!.email ?: o.user!!.phone ?: "",
            status = o.status.name,
            subtotalPrice = o.subtotalPrice,
            shippingPrice = o.shippingPrice,
            totalPrice = o.totalPrice,
            createdAt = o.createdAt,
            updatedAt = o.updatedAt,
            addressSnapshot = addrAny,
            items = o.items.map { it2 ->
                val options: Map<String, String>? = it2.optionsSnapshot?.let {
                    objectMapper.convertValue(it, object : TypeReference<Map<String, String>>() {})
                }
                AdminOrderItemResponse(
                    id = it2.id,
                    variantId = it2.variantId,
                    qty = it2.qty,
                    unitPriceSnapshot = it2.unitPriceSnapshot,
                    titleSnapshot = it2.titleSnapshot,
                    optionsSnapshot = options
                )
            }
        )
    }

    @Transactional
    fun updateStatus(orderId: Long, req: AdminUpdateOrderStatusRequest) {
        val newStatus = OrderStatus.valueOf(req.status.trim().uppercase())
        orderService.updateStatus(orderId, newStatus)
    }
}
