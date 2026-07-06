package com.kazemieh.shop.order.application

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.cart.api.dto.AddCartItemRequest
import com.kazemieh.shop.cart.application.CartService
import com.kazemieh.shop.cart.persistence.CartRepository
import com.kazemieh.shop.catalog.persistence.InventoryRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.customer.address.persistence.AddressRepository
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.order.api.dto.AdminUpdateShippingRequest
import com.kazemieh.shop.order.api.dto.CreateOrderRequest
import com.kazemieh.shop.order.api.dto.OrderDetailResponse
import com.kazemieh.shop.order.api.dto.OrderTrackingResponse
import com.kazemieh.shop.order.api.dto.ReorderResponse
import com.kazemieh.shop.order.api.mapper.OrderMapper
import com.kazemieh.shop.order.application.exception.*
import com.kazemieh.shop.order.persistence.OrderRepository
import com.kazemieh.shop.order.persistence.entity.OrderEntity
import com.kazemieh.shop.order.persistence.entity.OrderItemEntity
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.shared.error.ApiException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val cartRepository: CartRepository,
    private val cartService: CartService,
    private val objectMapper: ObjectMapper,
    private val walletService: com.kazemieh.shop.wallet.application.WalletService,
    private val courseAccessService: com.kazemieh.shop.academy.application.CourseAccessService,
    private val clinicAccessService: com.kazemieh.shop.clinic.application.ClinicAccessService,
    private val psychTestAccessService: com.kazemieh.shop.psychtest.application.PsychTestAccessService,
) {

    @Transactional(readOnly = true)
    fun listMyOrders(userId: Long) =
        orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map(OrderMapper::toOrderResponse)

    @Transactional(readOnly = true)
    fun getMyOrder(userId: Long, orderId: Long): OrderDetailResponse {
        val o = orderRepository.findByIdAndUserId(orderId, userId) ?: throw OrderNotFoundException(orderId)
        return OrderMapper.toDetailResponse(o, objectMapper)
    }
    
    @Transactional(readOnly = true)
    fun getOrderByIdForPayment(orderId: Long): OrderEntity {
        return orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
    }

    @Transactional(readOnly = true)
    fun trackOrder(orderId: Long): OrderTrackingResponse {
        val order = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }
        return OrderMapper.toOrderTrackingResponse(order)
    }

    /** سفارشِ مجددِ یک‌کلیکی: آیتم‌هایِ یک سفارشِ قبلی را به سبدِ فعلی اضافه می‌کند؛ آیتم‌هایِ
     * غیرفعال/ناموجود به‌آرامی رد می‌شوند و در پاسخ فهرست می‌شوند، بدونِ متوقف‌کردنِ کلِ عملیات. */
    @Transactional
    fun reorder(userId: Long, orderId: Long): ReorderResponse {
        val order = orderRepository.findByIdAndUserId(orderId, userId) ?: throw OrderNotFoundException(orderId)

        val skipped = mutableListOf<String>()
        var cartResponse = com.kazemieh.shop.cart.api.dto.CartResponse(
            items = emptyList(), savedForLater = emptyList(),
            subtotal = BigDecimal.ZERO, discountAmount = BigDecimal.ZERO, total = BigDecimal.ZERO, totalQty = 0
        )
        for (item in order.items) {
            try {
                cartResponse = cartService.addItem(userId, AddCartItemRequest(variantId = item.variantId, qty = item.qty))
            } catch (e: ApiException) {
                skipped.add(item.titleSnapshot)
            }
        }
        return ReorderResponse(cart = cartResponse, skippedTitles = skipped)
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
        
        // Find variants with their options to create options snapshot
        val variants = productVariantRepository.findWithAllOptionsByIds(variantIds).associateBy { it.id }

        // 2) validate variants + محاسبه subtotal
        var subtotal = BigDecimal.ZERO
        val normalizedItems = req.items.groupBy { it.variantId }.mapValues { (_, list) -> list.sumOf { it.qty } }

        for ((variantId, qty) in normalizedItems) {
            val v = variants[variantId] ?: throw VariantNotFoundException(variantId)
            if (!v.isActive) throw VariantInactiveException(variantId)

            val effectivePrice = v.discountedPrice ?: v.price
            subtotal = subtotal.add(effectivePrice.multiply(qty.toBigDecimal()))
        }

        val shipping = BigDecimal.ZERO
        val total = subtotal.add(shipping)

        // Wallet Logic
        var walletPaid = BigDecimal.ZERO
        if (req.useWallet) {
            val wallet = walletService.getOrCreateWallet(userId)
            walletPaid = total.min(wallet.balance)
            if (walletPaid > BigDecimal.ZERO) {
                walletService.addTransaction(
                    userId = userId,
                    amount = walletPaid.negate(),
                    type = com.kazemieh.shop.wallet.persistence.entity.TransactionType.PURCHASE,
                    description = "خرید محصول - کسر از کیف پول",
                    referenceId = null // Will update after order save
                )
            }
        }
        val gatewayPaid = total.subtract(walletPaid)

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
        val addressSnapshot: JsonNode = objectMapper.valueToTree(
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
            status = if (gatewayPaid > BigDecimal.ZERO) OrderStatus.PLACED else OrderStatus.PROCESSING,
            subtotalPrice = subtotal,
            shippingPrice = shipping,
            totalPrice = total,
            walletPaidAmount = walletPaid,
            gatewayPaidAmount = gatewayPaid,
            addressSnapshot = addressSnapshot,
            isGift = req.isGift,
            giftMessage = req.giftMessage?.takeIf { req.isGift }
        )

        for ((variantId, qty) in normalizedItems) {
            val v = variants.getValue(variantId)
            val effectivePrice = v.discountedPrice ?: v.price
            val optionsSnapshotMap = v.optionValues.associate { it.optionType.name to it.value }
            val optionsSnapshot = objectMapper.valueToTree<JsonNode>(optionsSnapshotMap)
            
            order.items.add(
                OrderItemEntity(
                    order = order,
                    variantId = variantId,
                    qty = qty,
                    unitPriceSnapshot = effectivePrice,
                    titleSnapshot = v.product?.title ?: "",
                    optionsSnapshot = optionsSnapshot
                )
            )
        }

        order.recordStatus(order.status)
        val saved = orderRepository.save(order)

        // اگر سفارش با کیف‌پول کامل پرداخت شد (مستقیم PROCESSING شد)، دسترسیِ دیجیتال را اعطا کن.
        if (saved.status == OrderStatus.PROCESSING) {
            val productQty = mutableMapOf<Long, Int>()
            for ((variantId, qty) in normalizedItems) {
                val productId = variants[variantId]?.product?.id ?: continue
                productQty[productId] = (productQty[productId] ?: 0) + qty
            }
            courseAccessService.grantAccessForProducts(userId, productQty.keys)
            clinicAccessService.grantSessionCredits(userId, productQty)
            psychTestAccessService.grantTestAccess(userId, productQty)
        }

        return OrderMapper.toDetailResponse(saved, objectMapper)
    }

    @Transactional
    fun clearCartAfterSuccessfulPayment(orderId: Long) {
        val order = orderRepository.findById(orderId).orElse(null)
        val userId = order?.user?.id
        if (userId != null) {
            cartRepository.findWithItemsByUserId(userId)?.let { cart ->
                cart.items.clear()
                cart.discount = null
                cartRepository.save(cart)
            }
        }
    }

    @Transactional
    fun cancelMyOrder(userId: Long, orderId: Long) {
        val o = orderRepository.findByIdAndUserId(orderId, userId) ?: throw OrderNotFoundException(orderId)

        if (o.status != OrderStatus.PLACED && o.status != OrderStatus.PROCESSING) {
            throw OrderStatusNotAllowedException()
        }

        // release reserved
        val variantIds = o.items.map { it.variantId }.distinct()
        val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }

        for (it in o.items) {
            val inv = invRows[it.variantId] ?: continue
            inv.reserved = (inv.reserved - it.qty).coerceAtLeast(0)
        }

        o.status = OrderStatus.CANCELLED
        o.recordStatus(OrderStatus.CANCELLED)
    }

    // --- Admin / system status update ---
    @Transactional
    fun updateStatus(orderId: Long, newStatus: OrderStatus) {
        val o = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }

        val allowed = when (o.status) {
            OrderStatus.PLACED -> newStatus in setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED)
            OrderStatus.PROCESSING -> newStatus in setOf(OrderStatus.SHIPPING, OrderStatus.CANCELLED)
            OrderStatus.SHIPPING -> newStatus == OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> false
            OrderStatus.CANCELLED -> false
        }
        if (!allowed) throw OrderStatusNotAllowedException()

        if (newStatus == OrderStatus.COMPLETED) {
            val variantIds = o.items.map { it.variantId }.distinct()
            val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }

            for (it in o.items) {
                val inv = invRows[it.variantId] ?: throw NotEnoughStockException(it.variantId)
                if (inv.reserved < it.qty || inv.onHand < it.qty) throw NotEnoughStockException(it.variantId)

                inv.reserved -= it.qty
                inv.onHand -= it.qty
            }
        }

        if (newStatus == OrderStatus.CANCELLED && o.status in setOf(OrderStatus.PLACED, OrderStatus.PROCESSING)) {
            val variantIds = o.items.map { it.variantId }.distinct()
            val invRows = inventoryRepository.findAllForUpdate(variantIds).associateBy { it.variantId }
            for (it in o.items) {
                invRows[it.variantId]?.let { inv ->
                    inv.reserved = (inv.reserved - it.qty).coerceAtLeast(0)
                }
            }
        }

        if (newStatus == OrderStatus.COMPLETED) {
            o.deliveredAt = OffsetDateTime.now()
        }

        // پرداختِ درگاه: با تأییدِ پرداخت سفارش به PROCESSING می‌رود ⇒ اعطای دسترسیِ دیجیتال (دوره‌ها/اعتبارِ جلسه).
        if (newStatus == OrderStatus.PROCESSING) {
            o.user?.id?.let { uid ->
                val variantIds = o.items.map { it.variantId }.distinct()
                val variantsById = productVariantRepository.findWithAllOptionsByIds(variantIds).associateBy { it.id }
                val productQty = mutableMapOf<Long, Int>()
                for (item in o.items) {
                    val productId = variantsById[item.variantId]?.product?.id ?: continue
                    productQty[productId] = (productQty[productId] ?: 0) + item.qty
                }
                courseAccessService.grantAccessForProducts(uid, productQty.keys)
                clinicAccessService.grantSessionCredits(uid, productQty)
                psychTestAccessService.grantTestAccess(uid, productQty)
            }
        }

        o.status = newStatus
        o.recordStatus(newStatus)
    }

    @Transactional
    fun updateShipping(orderId: Long, req: AdminUpdateShippingRequest) {
        val o = orderRepository.findById(orderId).orElseThrow { OrderNotFoundException(orderId) }

        if (req.shippingCarrier != null) o.shippingCarrier = req.shippingCarrier.trim().ifBlank { null }
        if (req.trackingCode != null) o.trackingCode = req.trackingCode.trim().ifBlank { null }

        if (req.markShipped && o.status == OrderStatus.PROCESSING) {
            o.status = OrderStatus.SHIPPING
            o.shippedAt = OffsetDateTime.now()
            o.recordStatus(OrderStatus.SHIPPING)
        }
    }
}