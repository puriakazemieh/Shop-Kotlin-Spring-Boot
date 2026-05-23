package com.kazemieh.shop.order.api.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.order.api.dto.AddressSnapshotResponse
import com.kazemieh.shop.order.api.dto.OrderDetailResponse
import com.kazemieh.shop.order.api.dto.OrderItemResponse
import com.kazemieh.shop.order.api.dto.OrderResponse
import com.kazemieh.shop.order.persistence.entity.OrderEntity

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
            createdAt = o.createdAt,
            address = addr,
            items = o.items.map {
                val options: Map<String, String> = om.convertValue(it.optionsSnapshot, Map::class.java) as Map<String, String>
                OrderItemResponse(
                    id = it.id,
                    variantId = it.variantId,
                    qty = it.qty,
                    unitPrice = it.unitPriceSnapshot,
                    title = it.titleSnapshot,
                    options = options
                )
            }
        )
    }

    private fun parseAddressSnapshot(node: JsonNode, om: ObjectMapper): AddressSnapshotResponse =
        om.treeToValue(node, AddressSnapshotResponse::class.java)
}
