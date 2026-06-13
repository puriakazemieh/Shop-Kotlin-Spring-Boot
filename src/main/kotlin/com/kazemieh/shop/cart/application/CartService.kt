package com.kazemieh.shop.cart.application

import com.kazemieh.shop.cart.api.dto.*
import com.kazemieh.shop.cart.application.exception.*
import com.kazemieh.shop.cart.persistence.CartItemRepository
import com.kazemieh.shop.cart.persistence.CartRepository
import com.kazemieh.shop.cart.persistence.entity.CartEntity
import com.kazemieh.shop.cart.persistence.entity.CartItemEntity
import com.kazemieh.shop.catalog.persistence.InventoryRepository
import com.kazemieh.shop.catalog.persistence.ProductImageRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.discount.persistence.DiscountRepository
import com.kazemieh.shop.discount.persistence.entity.DiscountEntity
import com.kazemieh.shop.discount.persistence.entity.DiscountType
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val userRepository: UserRepository,
    private val variantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val imageRepository: ProductImageRepository,
    private val discountRepository: DiscountRepository
) {

    @Transactional
    fun getCart(userId: Long): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        return buildCartResponse(cart)
    }

    @Transactional
    fun addItem(userId: Long, req: AddCartItemRequest): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)

        val variant = variantRepository.findById(req.variantId).orElseThrow { VariantNotFoundException(req.variantId) }
        if (!variant.isActive) throw VariantInactiveException(req.variantId)

        val current = cartItemRepository.findByCartIdAndVariantId(cart.id, req.variantId)
        val newQty = (current?.qty ?: 0) + req.qty

        ensureStock(req.variantId, newQty)

        if (current != null) {
            current.qty = newQty
            current.savedForLater = false
        } else {
            cart.items.add(
                CartItemEntity(
                    cart = cart,
                    variantId = req.variantId,
                    qty = req.qty
                )
            )
        }

        return buildCartResponse(cart)
    }

    @Transactional
    fun updateItemQty(userId: Long, itemId: Long, req: UpdateCartItemRequest): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        val item = cartItemRepository.findByIdAndCartId(itemId, cart.id) ?: throw CartItemNotFoundException(itemId)

        if (req.qty <= 0) {
            cart.items.removeIf { it.id == itemId }
            cartItemRepository.delete(item)
            return buildCartResponse(cart)
        }

        val variant =
            variantRepository.findById(item.variantId).orElseThrow { VariantNotFoundException(item.variantId) }
        if (!variant.isActive) throw VariantInactiveException(item.variantId)

        ensureStock(item.variantId, req.qty)
        item.qty = req.qty

        return buildCartResponse(cart)
    }

    @Transactional
    fun removeItem(userId: Long, itemId: Long): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        val item = cartItemRepository.findByIdAndCartId(itemId, cart.id) ?: throw CartItemNotFoundException(itemId)
        cart.items.removeIf { it.id == itemId }
        cartItemRepository.delete(item)
        return buildCartResponse(cart)
    }

    @Transactional
    fun clear(userId: Long): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        cart.items.clear()
        cart.discount = null
        cartItemRepository.deleteAllByCartId(cart.id)
        return buildCartResponse(cart)
    }

    @Transactional
    fun saveForLater(userId: Long, itemId: Long): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        val item = cartItemRepository.findByIdAndCartId(itemId, cart.id) ?: throw CartItemNotFoundException(itemId)
        item.savedForLater = true
        return buildCartResponse(cart)
    }

    @Transactional
    fun moveToCart(userId: Long, itemId: Long): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        val item = cartItemRepository.findByIdAndCartId(itemId, cart.id) ?: throw CartItemNotFoundException(itemId)
        item.savedForLater = false
        return buildCartResponse(cart)
    }

    @Transactional
    fun setVariantQty(userId: Long, variantId: Long, req: SetCartVariantQtyRequest): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)

        if (req.qty <= 0) {
            val existing = cartItemRepository.findByCartIdAndVariantId(cart.id, variantId)
            if (existing != null) {
                cart.items.removeIf { it.id == existing.id }
                cartItemRepository.delete(existing)
            }
            return buildCartResponse(cart)
        }

        val variant = variantRepository.findById(variantId).orElseThrow { VariantNotFoundException(variantId) }
        if (!variant.isActive) throw VariantInactiveException(variantId)

        ensureStock(variantId, req.qty)

        val existing = cartItemRepository.findByCartIdAndVariantId(cart.id, variantId)
        if (existing != null) {
            existing.qty = req.qty
            existing.savedForLater = false
        } else {
            cart.items.add(
                CartItemEntity(
                    cart = cart,
                    variantId = variantId,
                    qty = req.qty
                )
            )
        }

        return buildCartResponse(cart)
    }

    @Transactional
    fun adjustVariantQty(userId: Long, variantId: Long, req: AdjustCartVariantQtyRequest): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)

        val existing = cartItemRepository.findByCartIdAndVariantId(cart.id, variantId)
        val currentQty = existing?.qty ?: 0
        val newQty = currentQty + req.delta

        if (newQty <= 0) {
            if (existing != null) {
                cart.items.removeIf { it.id == existing.id }
                cartItemRepository.delete(existing)
            }
            return buildCartResponse(cart)
        }

        val variant = variantRepository.findById(variantId).orElseThrow { VariantNotFoundException(variantId) }
        if (!variant.isActive) throw VariantInactiveException(variantId)

        ensureStock(variantId, newQty)

        if (existing != null) {
            existing.qty = newQty
            existing.savedForLater = false
        } else {
            cart.items.add(
                CartItemEntity(
                    cart = cart,
                    variantId = variantId,
                    qty = newQty
                )
            )
        }

        return buildCartResponse(cart)
    }

    @Transactional
    fun applyDiscount(userId: Long, req: ApplyDiscountRequest): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        val discount = discountRepository.findByCode(req.code)
            .orElseThrow { DiscountNotFoundException(req.code) }

        validateDiscount(discount, cart)

        cart.discount = discount
        return buildCartResponse(cart)
    }

    @Transactional
    fun removeDiscount(userId: Long): CartResponse {
        val cart = cartRepository.findWithItemsByUserId(userId) ?: ensureCart(userId)
        cart.discount = null
        return buildCartResponse(cart)
    }

    // ---------------- Helpers ----------------

    private fun ensureCart(userId: Long): CartEntity {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        return cartRepository.findByUserId(userId) ?: cartRepository.save(CartEntity(user = user))
    }

    private fun ensureStock(variantId: Long, requestedQty: Int) {
        val inv = inventoryRepository.findById(variantId).orElse(null)
        val available = ((inv?.onHand ?: 0) - (inv?.reserved ?: 0)).coerceAtLeast(0)
        if (requestedQty > available) throw NotEnoughStockForCartException(variantId, requestedQty, available)
    }

    private fun validateDiscount(discount: DiscountEntity, cart: CartEntity) {
        if (!discount.isActive) throw DiscountNotFoundException(discount.code)
        if (discount.endDate != null && discount.endDate!!.isBefore(OffsetDateTime.now())) throw DiscountExpiredException(discount.code)
        if (discount.startDate != null && discount.startDate!!.isAfter(OffsetDateTime.now())) throw DiscountNotStartedException(discount.code)
        if (discount.usageLimit != null && discount.usageCount >= discount.usageLimit!!) throw DiscountUsageLimitExceededException(discount.code)

        val subtotal = calculateSubtotal(cart)
        if (discount.minOrderAmount != null && subtotal < discount.minOrderAmount) throw DiscountMinOrderAmountNotMetException(discount.code)
    }

    private fun calculateSubtotal(cart: CartEntity): BigDecimal {
        var subtotal = BigDecimal.ZERO
        val variantIds = cart.items.filter { !it.savedForLater }.map { it.variantId }
        if (variantIds.isEmpty()) return subtotal
        
        val variants = variantRepository.findAllById(variantIds).associateBy { it.id }

        for (item in cart.items) {
            if (!item.savedForLater) {
                val variant = variants[item.variantId] ?: continue
                subtotal = subtotal.add(variant.price.multiply(item.qty.toBigDecimal()))
            }
        }
        return subtotal
    }

    private fun buildCartResponse(cart: CartEntity): CartResponse {
        if (cart.items.isEmpty()) return CartResponse(emptyList(), emptyList(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, null)

        val variantIds = cart.items.map { it.variantId }.distinct()
        val variants = variantRepository.findWithAllOptionsByIds(variantIds).associateBy { it.id }
        val invMap = inventoryRepository.findAllById(variantIds).associateBy { it.variantId }
        val productIds = variants.values.map { it.product!!.id }.distinct()
        val images = if (productIds.isNotEmpty()) imageRepository.findAllByProductIdInOrderBySortOrderAsc(productIds) else emptyList()
        val thumbByProductId = images.groupBy { it.product?.id ?: 0L }.mapValues { it.value.firstOrNull()?.url }

        var subtotal = BigDecimal.ZERO
        var totalQty = 0

        val allItems = cart.items.map { ci ->
            val v = variants[ci.variantId] ?: throw VariantNotFoundException(ci.variantId)
            if (!v.isActive) throw VariantInactiveException(ci.variantId)
            val inv = invMap[ci.variantId]
            val available = ((inv?.onHand ?: 0) - (inv?.reserved ?: 0)).coerceAtLeast(0)
            val lineTotal = v.price.multiply(ci.qty.toBigDecimal())
            if (!ci.savedForLater) {
                subtotal = subtotal.add(lineTotal)
                totalQty += ci.qty
            }
            val options = v.optionValues.associate { it.optionType.name to it.value }
            CartItemResponse(
                id = ci.id, variantId = ci.variantId, qty = ci.qty, savedForLater = ci.savedForLater,
                productId = v.product!!.id, productTitle = v.product!!.title, productSlug = v.product!!.slug, imageUrl = thumbByProductId[v.product!!.id],
                options = options, price = v.price, compareAtPrice = v.compareAtPrice, availableQty = available, isActive = v.isActive, lineTotal = lineTotal
            )
        }

        val (items, saved) = allItems.partition { !it.savedForLater }

        var discountAmount = BigDecimal.ZERO
        var total = subtotal
        var appliedDiscountCode: String? = null

        cart.discount?.let { disc ->
            try {
                validateDiscount(disc, cart) // Re-validate in case cart changed
                when (disc.type) {
                    DiscountType.PERCENTAGE -> {
                        val calculatedDiscount = subtotal.multiply(disc.value.divide(BigDecimal(100)))
                        discountAmount = if (disc.maxDiscountAmount != null && calculatedDiscount > disc.maxDiscountAmount) {
                            disc.maxDiscountAmount!!
                        } else {
                            calculatedDiscount
                        }
                    }
                    DiscountType.FIXED_AMOUNT -> {
                        discountAmount = disc.value
                    }
                }
                // Ensure discount doesn't make total negative
                if (discountAmount > subtotal) {
                    discountAmount = subtotal
                }
                total = subtotal.subtract(discountAmount)
                appliedDiscountCode = disc.code

            } catch (e: RuntimeException) {
                // If discount is no longer valid, remove it
                cart.discount = null
                // No discount applied
            }
        }

        return CartResponse(
            items = items,
            savedForLater = saved,
            subtotal = subtotal,
            discountAmount = discountAmount,
            total = total,
            totalQty = totalQty,
            appliedDiscountCode = appliedDiscountCode
        )
    }
}
