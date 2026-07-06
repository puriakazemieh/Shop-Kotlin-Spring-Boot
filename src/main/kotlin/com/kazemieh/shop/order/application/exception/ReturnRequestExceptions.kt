package com.kazemieh.shop.order.application.exception

import com.kazemieh.shop.shared.error.ApiException
import org.springframework.http.HttpStatus

class ReturnRequestNotFoundException(id: Long) :
    ApiException("Return request not found: $id", "RETURN_REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND)

class OrderAccessDeniedException(orderId: Long) :
    ApiException("Order access denied: $orderId", "ORDER_ACCESS_DENIED", HttpStatus.FORBIDDEN)

class OrderItemNotFoundException(id: Long) :
    ApiException("Order item not found: $id", "ORDER_ITEM_NOT_FOUND", HttpStatus.NOT_FOUND)

class OrderItemNotEligibleForReturnException(orderItemId: Long) :
    ApiException("Order item is not eligible for return: $orderItemId", "ORDER_ITEM_NOT_ELIGIBLE", HttpStatus.UNPROCESSABLE_ENTITY)

class ReturnRequestAlreadyExistsException(orderItemId: Long) :
    ApiException("An open return request already exists for order item: $orderItemId", "RETURN_REQUEST_EXISTS", HttpStatus.CONFLICT)
