package com.kazemieh.shop.cart.application

import com.kazemieh.shop.cart.api.dto.*
import com.kazemieh.shop.cart.application.exception.CartItemNotFoundException
import com.kazemieh.shop.cart.application.exception.NotEnoughStockForCartException
import com.kazemieh.shop.cart.application.exception.VariantInactiveException
import com.kazemieh.shop.cart.application.exception.VariantNotFoundException
import com.kazemieh.shop.cart.persistence.CartItemRepository
import com.kazemieh.shop.cart.persistence.CartRepository
import com.kazemieh.shop.cart.persistence.entity.CartEntity
import com.kazemieh.shop.cart.persistence.entity.CartItemEntity
import com.kazemieh.shop.catalog.persistence.InventoryRepository
import com.kazemieh.shop.catalog.persistence.ProductImageRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val userRepository: UserRepository,

    private val variantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val imageRepository: ProductImageRepository,
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
        cartItemRepository.deleteAllByCartId(cart.id)
        return CartResponse(items = emptyList(), subtotal = BigDecimal.ZERO, totalQty = 0)
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

    private fun buildCartResponse(cart: CartEntity): CartResponse {
        if (cart.items.isEmpty()) return CartResponse(emptyList(), BigDecimal.ZERO, 0)

        val variantIds = cart.items.map { it.variantId }.distinct()
        val variants = variantRepository.findWithAllOptionsByIds(variantIds).associateBy { it.id }

        val invMap = inventoryRepository.findAllById(variantIds).associateBy { it.variantId }

        // thumbnail: اولین تصویر هر محصول
        val productIds = variants.values.map { it.product!!.id }.distinct()
        val images = if (productIds.isNotEmpty())
            imageRepository.findAllByProductIdInOrderBySortOrderAsc(productIds)
        else emptyList()
        val thumbByProductId = images.groupBy { it.product?.id ?: 0L }.mapValues { it.value.firstOrNull()?.url }

        var subtotal = BigDecimal.ZERO
        var totalQty = 0

        val items = cart.items.map { ci ->
            val v = variants[ci.variantId] ?: throw VariantNotFoundException(ci.variantId)
            if (!v.isActive) throw VariantInactiveException(ci.variantId)

            val inv = invMap[ci.variantId]
            val available = ((inv?.onHand ?: 0) - (inv?.reserved ?: 0)).coerceAtLeast(0)

            val lineTotal = v.price.multiply(ci.qty.toBigDecimal())
            subtotal = subtotal.add(lineTotal)
            totalQty += ci.qty

            CartItemResponse(
                id = ci.id,
                variantId = ci.variantId,
                qty = ci.qty,

                productId = v.product!!.id,
                productTitle = v.product!!.title,
                productSlug = v.product!!.slug,
                imageUrl = thumbByProductId[v.product!!.id],

                options = v.optionValues.associate { it.optionType.name to it.value },

                price = v.price,
                compareAtPrice = v.compareAtPrice,
                availableQty = available,
                isActive = v.isActive,

                lineTotal = lineTotal
            )
        }

        return CartResponse(items = items, subtotal = subtotal, totalQty = totalQty)
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
}
