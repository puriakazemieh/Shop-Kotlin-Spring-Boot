package com.kazemieh.shop.cart.application.exception

import com.kazemieh.shop.shared.error.ApiException
import org.springframework.http.HttpStatus

class CartItemNotFoundException(id: Long) :
    ApiException("Cart item not found: $id", "CART_ITEM_NOT_FOUND", HttpStatus.NOT_FOUND)

class VariantNotFoundException(id: Long) :
    ApiException("Variant not found: $id", "VARIANT_NOT_FOUND", HttpStatus.NOT_FOUND)

class VariantInactiveException(id: Long) :
    ApiException("Variant is inactive: $id", "VARIANT_INACTIVE", HttpStatus.BAD_REQUEST)

class NotEnoughStockForCartException(variantId: Long, requested: Int, available: Int) :
    ApiException(
        "Not enough stock for variant $variantId (requested=$requested, available=$available)",
        "NOT_ENOUGH_STOCK",
        HttpStatus.CONFLICT
    )