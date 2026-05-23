package com.kazemieh.shop.shared.error

import org.springframework.http.HttpStatus

open class ApiException(
    message: String,
    val errorCode: String,
    val httpStatus: HttpStatus
) : RuntimeException(message)

// Base groups
open class NotFoundException(message: String, code: String) : ApiException(message, code, HttpStatus.NOT_FOUND)
open class ConflictException(message: String, code: String) : ApiException(message, code, HttpStatus.CONFLICT)
open class BadRequestException(message: String, code: String) : ApiException(message, code, HttpStatus.BAD_REQUEST)
open class UnauthorizedException(message: String, code: String) : ApiException(message, code, HttpStatus.UNAUTHORIZED)
open class ForbiddenException(message: String, code: String) : ApiException(message, code, HttpStatus.FORBIDDEN)
open class UnprocessableException(message: String, code: String) : ApiException(message, code, HttpStatus.UNPROCESSABLE_ENTITY)

// ===== Users / Auth =====
//class UserNotFoundException(message: String = "User not found")
//    : NotFoundException(message, ErrorCodes.USER_NOT_FOUND)
//
//class EmailAlreadyExistsException(email: String)
//    : ConflictException("Email already exists: $email", ErrorCodes.EMAIL_ALREADY_EXISTS)
//
//class UserInactiveException(message: String = "User is inactive")
//    : ForbiddenException(message, ErrorCodes.USER_INACTIVE)
//
//class InvalidCredentialsException(message: String = "Invalid credentials")
//    : UnauthorizedException(message, ErrorCodes.INVALID_CREDENTIALS)
//
//class ShopAccessDeniedException(message: String = "Access denied")
//    : ForbiddenException(message, ErrorCodes.ACCESS_DENIED)

// ===== Categories =====
class CategoryNotFoundException(message: String = "Category not found")
    : NotFoundException(message, ErrorCodes.CATEGORY_NOT_FOUND)

class CategorySlugAlreadyExistsException(slug: String)
    : ConflictException("Category slug already exists: $slug", ErrorCodes.CATEGORY_SLUG_EXISTS)

class CategoryCycleDetectedException(message: String = "Category cycle detected")
    : UnprocessableException(message, ErrorCodes.CATEGORY_CYCLE)

// ===== Products =====
class ProductNotFoundException(message: String = "Product not found")
    : NotFoundException(message, ErrorCodes.PRODUCT_NOT_FOUND)

class ProductSlugAlreadyExistsException(slug: String)
    : ConflictException("Product slug already exists: $slug", ErrorCodes.PRODUCT_SLUG_EXISTS)

class ProductInactiveException(productId: Long)
    : UnprocessableException("Product is inactive: $productId", ErrorCodes.PRODUCT_INACTIVE)

class InvalidProductPriceException(message: String = "Invalid product price")
    : UnprocessableException(message, ErrorCodes.INVALID_PRODUCT_PRICE)

// ===== Images =====
class ProductImageNotFoundException(message: String = "Product image not found")
    : NotFoundException(message, ErrorCodes.PRODUCT_IMAGE_NOT_FOUND)

class InvalidImageUrlException(url: String)
    : BadRequestException("Invalid image url: $url", ErrorCodes.INVALID_IMAGE_URL)

// ===== Variants =====
class VariantNotFoundException(message: String = "Variant not found")
    : NotFoundException(message, ErrorCodes.VARIANT_NOT_FOUND)

class SkuAlreadyExistsException(sku: String)
    : ConflictException("SKU already exists: $sku", ErrorCodes.SKU_EXISTS)

class VariantCombinationAlreadyExistsException(productId: Long, options: Map<String, String>)
    : ConflictException(
        "Variant already exists for product=$productId options=$options",
        ErrorCodes.VARIANT_COMBO_EXISTS
    )

class VariantInactiveException(variantId: Long)
    : UnprocessableException("Variant is inactive: $variantId", ErrorCodes.VARIANT_INACTIVE)

class InvalidVariantPriceException(message: String = "Invalid variant price")
    : UnprocessableException(message, ErrorCodes.INVALID_VARIANT_PRICE)

// ===== Inventory =====
class InventoryNotFoundException(variantId: Long)
    : NotFoundException("Inventory not found for variantId=$variantId", ErrorCodes.INVENTORY_NOT_FOUND)

class InsufficientStockException(variantId: Long, requested: Int, available: Int)
    : UnprocessableException(
        "Insufficient stock for variantId=$variantId requested=$requested available=$available",
        ErrorCodes.INSUFFICIENT_STOCK
    )

class InventoryReservationExceededException(variantId: Long)
    : UnprocessableException(
        "Reserved cannot be greater than on_hand for variantId=$variantId",
        ErrorCodes.INVALID_INVENTORY
    )

class NegativeInventoryOperationException(message: String = "Inventory values cannot be negative")
    : UnprocessableException(message, ErrorCodes.INVALID_INVENTORY)

class ConcurrentInventoryUpdateException(variantId: Long)
    : ConflictException(
        "Inventory update conflict (concurrent update) for variantId=$variantId",
        ErrorCodes.INVENTORY_CONFLICT
    )

// ===== Addresses =====
class AddressNotFoundException(addressId: Long)
    : NotFoundException("Address not found: $addressId", ErrorCodes.ADDRESS_NOT_FOUND)

class AddressAccessDeniedException(addressId: Long)
    : ForbiddenException("Address access denied: $addressId", ErrorCodes.ADDRESS_ACCESS_DENIED)

class DefaultAddressConflictException(userId: Long)
    : ConflictException("Default address conflict for userId=$userId", ErrorCodes.DEFAULT_ADDRESS_CONFLICT)

class InvalidAddressException(message: String = "Invalid address")
    : BadRequestException(message, ErrorCodes.INVALID_ADDRESS)

// ===== Orders =====
class OrderNotFoundException(orderId: Long)
    : NotFoundException("Order not found: $orderId", ErrorCodes.ORDER_NOT_FOUND)

class OrderAccessDeniedException(orderId: Long)
    : ForbiddenException("Order access denied: $orderId", ErrorCodes.ORDER_ACCESS_DENIED)

class EmptyOrderException
    : UnprocessableException("Order must have at least one item", ErrorCodes.EMPTY_ORDER)

class InvalidOrderStatusException(status: String)
    : BadRequestException("Invalid order status: $status", ErrorCodes.INVALID_ORDER_STATUS)

class OrderStatusTransitionException(from: String, to: String)
    : UnprocessableException("Invalid order status transition: $from -> $to", ErrorCodes.ORDER_STATUS_TRANSITION)

class OrderAlreadyFinalizedException(orderId: Long)
    : UnprocessableException("Order already finalized: $orderId", ErrorCodes.ORDER_FINALIZED)

class OrderPriceMismatchException(expected: String, actual: String)
    : UnprocessableException("Order price mismatch expected=$expected actual=$actual", ErrorCodes.ORDER_PRICE_MISMATCH)

// ===== Order Items =====
class OrderItemNotFoundException(itemId: Long)
    : NotFoundException("Order item not found: $itemId", ErrorCodes.ORDER_ITEM_NOT_FOUND)

class InvalidQuantityException(qty: Int)
    : BadRequestException("Invalid quantity: $qty", ErrorCodes.INVALID_QUANTITY)

class InvalidOrderItemPriceException(message: String = "Invalid order item price")
    : UnprocessableException(message, ErrorCodes.INVALID_ORDER_ITEM_PRICE)

class VariantNotAvailableForOrderException(variantId: Long)
    : UnprocessableException("Variant not available for order: $variantId", ErrorCodes.VARIANT_NOT_AVAILABLE)
