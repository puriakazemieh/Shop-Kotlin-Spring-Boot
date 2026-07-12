package com.kazemieh.shop.order.api.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.order.api.dto.*
import com.kazemieh.shop.order.persistence.entity.OrderEntity
import java.time.Instant

object OrderMapper {

    fun toOrderResponse(o: OrderEntity) = OrderResponse(
        id = o.id,
        status = o.status.name,
        subtotalPrice = o.subtotalPrice,
        shippingPrice = o.shippingPrice,
        totalPrice = o.totalPrice,
        createdAt = o.createdAt
    )

    fun toDetailResponse(o: OrderEntity, om: ObjectMapper): OrderDetailResponse {
        val addr = parseAddressSnapshot(o.addressSnapshot!!, om)

        return OrderDetailResponse(
            id = o.id,
            status = o.status.name,
            subtotalPrice = o.subtotalPrice,
            shippingPrice = o.shippingPrice,
            totalPrice = o.totalPrice,
            walletPaidAmount = o.walletPaidAmount,
            gatewayPaidAmount = o.gatewayPaidAmount,
            createdAt = o.createdAt,
            address = addr,
            items = o.items.map {
                val options: Map<String, String> =
                    om.convertValue(it.optionsSnapshot, Map::class.java) as Map<String, String>
                OrderItemResponse(
                    id = it.id,
                    variantId = it.variantId,
                    qty = it.qty,
                    unitPrice = it.unitPriceSnapshot,
                    title = it.titleSnapshot,
                    options = options
                )
            },
            isGift = o.isGift,
            giftMessage = o.giftMessage
        )
    }

    fun toOrderTrackingResponse(o: OrderEntity) = OrderTrackingResponse(
        id = o.id.toInt(),
        status = o.status,
        trackingCode = o.trackingCode,
        orderedAt = o.createdAt?.toInstant() ?: Instant.now(),
        shippedAt = o.shippedAt?.toInstant(),
        history = o.statusHistory.map {
            OrderStatusHistoryItem(status = it.status, at = it.at?.toInstant() ?: Instant.now())
        }
    )

    private fun parseAddressSnapshot(node: JsonNode, om: ObjectMapper): AddressSnapshotResponse =
        om.treeToValue(node, AddressSnapshotResponse::class.java)
}