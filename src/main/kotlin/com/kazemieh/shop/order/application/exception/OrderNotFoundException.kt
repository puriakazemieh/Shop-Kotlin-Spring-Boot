package com.kazemieh.shop.order.application.exception

import com.kazemieh.shop.shared.error.ApiException
import org.springframework.http.HttpStatus

class OrderNotFoundException(id: Long) :
    ApiException("Order not found: $id", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND)

class AddressNotFoundForUserException(id: Long) :
    ApiException("Address not found: $id", "ADDRESS_NOT_FOUND", HttpStatus.NOT_FOUND)

class EmptyOrderException :
    ApiException("Order items cannot be empty", "EMPTY_ORDER", HttpStatus.BAD_REQUEST)

class VariantNotFoundException(id: Long) :
    ApiException("Variant not found: $id", "VARIANT_NOT_FOUND", HttpStatus.NOT_FOUND)

class VariantInactiveException(id: Long) :
    ApiException("Variant is inactive: $id", "VARIANT_INACTIVE", HttpStatus.BAD_REQUEST)

class NotEnoughStockException(variantId: Long) :
    ApiException("Not enough stock for variant: $variantId", "NOT_ENOUGH_STOCK", HttpStatus.CONFLICT)

class OrderStatusNotAllowedException :
    ApiException("Order status change not allowed", "ORDER_STATUS_NOT_ALLOWED", HttpStatus.BAD_REQUEST)