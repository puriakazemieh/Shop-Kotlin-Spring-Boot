package com.kazemieh.shop.order.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.catalog.persistence.InventoryRepository
import com.kazemieh.shop.catalog.persistence.VariantQueryRepository
import com.kazemieh.shop.customer.address.persistence.AddressRepository
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.order.api.dto.CreateOrderRequest
import com.kazemieh.shop.order.api.dto.OrderDetailResponse
import com.kazemieh.shop.order.api.mapper.OrderMapper
import com.kazemieh.shop.order.application.exception.AddressNotFoundForUserException
import com.kazemieh.shop.order.application.exception.EmptyOrderException
import com.kazemieh.shop.order.application.exception.NotEnoughStockException
import com.kazemieh.shop.order.application.exception.OrderNotFoundException
import com.kazemieh.shop.order.application.exception.OrderStatusNotAllowedException
import com.kazemieh.shop.order.application.exception.VariantInactiveException
import com.kazemieh.shop.order.application.exception.VariantNotFoundException
import com.kazemieh.shop.order.persistence.OrderRepository
import com.kazemieh.shop.order.persistence.entity.OrderEntity
import com.kazemieh.shop.order.persistence.entity.OrderItemEntity
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.collections.iterator

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val variantQueryRepository: VariantQueryRepository,
    private val inventoryRepository: InventoryRepository,
    private val objectMapper: ObjectMapper,
) {

    @Transactional(readOnly = true)
    fun listMyOrders(userId: Long) =
        orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map(OrderMapper::toOrderResponse)

    @Transactional(readOnly = true)
    fun getMyOrder(userId: Long, orderId: Long) : OrderDetailResponse {
        val o = orderRepository.findByIdAndUserId(orderId, userId) ?: throw OrderNotFoundException(orderId)
        return OrderMapper.toDetailResponse(o, objectMapper)
    }

    @Transactional
    fun create(userId: Long, req: CreateOrderRequest): OrderDetailResponse {
        if (req.items.isEmpty()) throw EmptyOrderException()

        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        // 1) آدرس را پیدا کن (یا default)
        val address = when (val aid = req.addressId) {
            null -> addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
                ?: throw AddressNotFoundForUserException(-1)
            else -> addressRepository.findById(aid).orElse(null)?.takeIf { it.user?.id == userId }
                ?: throw AddressNotFoundForUserException(aid)
        }

        val variantIds = req.items.map { it.variantId }.distinct()
        val snapshots = variantQueryRepository.findSnapshots(variantIds).associateBy { it.getVariantId() }

        // 2) validate variants + محاسبه subtotal
        var subtotal = BigDecimal.ZERO
        val normalizedItems = req.items.groupBy { it.variantId }.mapValues { (_, list) -> list.sumOf { it.qty } }

        for ((variantId, qty) in normalizedItems) {
            val s = snapshots[variantId] ?: throw VariantNotFoundException(variantId)
            if (!s.getIsActive()) throw VariantInactiveException(variantId)

            subtotal = subtotal.add(s.getPrice().multiply(qty.toBigDecimal()))
        }

        val shipping = BigDecimal.ZERO
        val total = subtotal.add(shipping)

        // 3) lock inventory rows + reserve
        val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }
        for ((variantId, qty) in normalizedItems) {
            val inv = invRows[variantId] ?: throw NotEnoughStockException(variantId)
            val available = inv.onHand - inv.reserved
            if (available < qty) throw NotEnoughStockException(variantId)
        }
        for ((variantId, qty) in normalizedItems) {
            val inv = invRows.getValue(variantId)
            inv.reserved += qty
        }

        // 4) create order + items snapshots
        val addressSnapshot = objectMapper.valueToTree<Map<String, Any?>>(
            mapOf(
                "receiverName" to address.receiverName,
                "receiverPhone" to address.receiverPhone,
                "country" to address.country,
                "province" to address.province,
                "city" to address.city,
                "addressLine1" to address.addressLine1,
                "addressLine2" to address.addressLine2,
                "postalCode" to address.postalCode,
            )
        )

        val order = OrderEntity(
            user = user,
            status = OrderStatus.PENDING,
            subtotalPrice = subtotal,
            shippingPrice = shipping,
            totalPrice = total,
            addressSnapshot = addressSnapshot
        )

        for ((variantId, qty) in normalizedItems) {
            val s = snapshots.getValue(variantId)
            order.items.add(
                OrderItemEntity(
                    order = order,
                    variantId = variantId,
                    qty = qty,
                    unitPriceSnapshot = s.getPrice(),
                    titleSnapshot = s.getTitle(),
                    sizeSnapshot = s.getSizeName(),
                    colorSnapshot = s.getColorName()
                )
            )
        }

        val saved = orderRepository.save(order)
        return OrderMapper.toDetailResponse(saved, objectMapper)
    }

    @Transactional
    fun cancelMyOrder(userId: Long, orderId: Long) {
        val o = orderRepository.findByIdAndUserId(orderId, userId) ?: throw OrderNotFoundException(orderId)

        if (o.status != OrderStatus.PENDING && o.status != OrderStatus.CONFIRMED) {
            throw OrderStatusNotAllowedException()
        }

        // release reserved
        val variantIds = o.items.map { it.variantId }.distinct()
        val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }

        for (it in o.items) {
            val inv = invRows[it.variantId] ?: continue
            inv.reserved = (inv.reserved - it.qty).coerceAtLeast(0)
        }

        o.status = OrderStatus.CANCELED
    }

    // --- Admin / system status update ---
    @Transactional
    fun updateStatus(orderId: Long, newStatus: OrderStatus) {
        val o = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }

        // قوانین ساده:
        // PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
        // PENDING/CONFIRMED -> CANCELED
        val allowed = when (o.status) {
            OrderStatus.PENDING -> newStatus in setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELED)
            OrderStatus.CONFIRMED -> newStatus in setOf(OrderStatus.SHIPPED, OrderStatus.CANCELED)
            OrderStatus.SHIPPED -> newStatus == OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> false
            OrderStatus.CANCELED -> false
        }
        if (!allowed) throw OrderStatusNotAllowedException()

        // اگر به DELIVERED رفت: از رزرو کم کن و از موجودی کم کن
        if (newStatus == OrderStatus.DELIVERED) {
            val variantIds = o.items.map { it.variantId }.distinct()
            val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }

            for (it in o.items) {
                val inv = invRows[it.variantId] ?: throw NotEnoughStockException(it.variantId)
                // چون قبلاً reserved شده، باید حداقل reserved >= qty باشد
                if (inv.reserved < it.qty || inv.onHand < it.qty) throw NotEnoughStockException(it.variantId)

                inv.reserved -= it.qty
                inv.onHand -= it.qty
            }
        }

        // اگر از CONFIRMED/PENDING به CANCELED رفت: رزرو آزاد شود
        if (newStatus == OrderStatus.CANCELED) {
            val variantIds = o.items.map { it.variantId }.distinct()
            val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }
            for (it in o.items) {
                invRows[it.variantId]?.let { inv ->
                    inv.reserved = (inv.reserved - it.qty).coerceAtLeast(0)
                }
            }
        }

        o.status = newStatus
    }
}