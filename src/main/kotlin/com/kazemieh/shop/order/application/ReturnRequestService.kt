package com.kazemieh.shop.order.application

import com.kazemieh.shop.order.api.dto.AdminReturnRequestResponse
import com.kazemieh.shop.order.api.dto.AdminUpdateReturnRequestRequest
import com.kazemieh.shop.order.api.dto.CreateReturnRequestRequest
import com.kazemieh.shop.order.api.dto.ReturnRequestResponse
import com.kazemieh.shop.order.application.exception.OrderAccessDeniedException
import com.kazemieh.shop.order.application.exception.OrderItemNotEligibleForReturnException
import com.kazemieh.shop.order.application.exception.OrderItemNotFoundException
import com.kazemieh.shop.order.application.exception.ReturnRequestAlreadyExistsException
import com.kazemieh.shop.order.application.exception.ReturnRequestNotFoundException
import com.kazemieh.shop.order.persistence.OrderItemRepository
import com.kazemieh.shop.order.persistence.ReturnRequestRepository
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.order.persistence.entity.ReturnRequestEntity
import com.kazemieh.shop.order.persistence.entity.ReturnRequestStatus
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ReturnRequestService(
    private val returnRequestRepository: ReturnRequestRepository,
    private val orderItemRepository: OrderItemRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(userId: Long, req: CreateReturnRequestRequest): ReturnRequestResponse {
        val orderItem = orderItemRepository.findById(req.orderItemId).orElseThrow { OrderItemNotFoundException(req.orderItemId) }
        val order = orderItem.order ?: throw OrderItemNotFoundException(req.orderItemId)
        if (order.user?.id != userId) throw OrderAccessDeniedException(order.id)
        if (order.status != OrderStatus.COMPLETED) throw OrderItemNotEligibleForReturnException(req.orderItemId)
        if (returnRequestRepository.existsByOrderItemIdAndStatusNot(req.orderItemId, ReturnRequestStatus.REJECTED)) {
            throw ReturnRequestAlreadyExistsException(req.orderItemId)
        }

        val user = userRepository.findById(userId).orElseThrow { OrderAccessDeniedException(order.id) }
        val saved = returnRequestRepository.save(
            ReturnRequestEntity(
                order = order,
                orderItem = orderItem,
                user = user,
                type = req.type,
                reason = req.reason
            )
        )
        return toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<ReturnRequestResponse> =
        returnRequestRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map(::toResponse)

    @Transactional(readOnly = true)
    fun adminList(): List<AdminReturnRequestResponse> =
        returnRequestRepository.findAllByOrderByCreatedAtDesc().map(::toAdminResponse)

    @Transactional
    fun adminUpdateStatus(id: Long, req: AdminUpdateReturnRequestRequest): AdminReturnRequestResponse {
        val entity = returnRequestRepository.findById(id).orElseThrow { ReturnRequestNotFoundException(id) }
        entity.status = req.status
        entity.adminNote = req.adminNote
        if (req.status == ReturnRequestStatus.APPROVED || req.status == ReturnRequestStatus.REJECTED || req.status == ReturnRequestStatus.COMPLETED) {
            entity.resolvedAt = OffsetDateTime.now()
        }
        return toAdminResponse(returnRequestRepository.save(entity))
    }

    private fun toResponse(e: ReturnRequestEntity) = ReturnRequestResponse(
        id = e.id,
        orderId = e.order?.id ?: 0,
        orderItemId = e.orderItem?.id ?: 0,
        itemTitle = e.orderItem?.titleSnapshot ?: "",
        type = e.type,
        reason = e.reason,
        status = e.status,
        adminNote = e.adminNote,
        createdAt = e.createdAt,
        resolvedAt = e.resolvedAt
    )

    private fun toAdminResponse(e: ReturnRequestEntity) = AdminReturnRequestResponse(
        id = e.id,
        orderId = e.order?.id ?: 0,
        orderItemId = e.orderItem?.id ?: 0,
        itemTitle = e.orderItem?.titleSnapshot ?: "",
        userId = e.user?.id ?: 0,
        userName = listOfNotNull(e.user?.firstName, e.user?.lastName).joinToString(" ").ifBlank { null },
        type = e.type,
        reason = e.reason,
        status = e.status,
        adminNote = e.adminNote,
        createdAt = e.createdAt,
        resolvedAt = e.resolvedAt
    )
}
