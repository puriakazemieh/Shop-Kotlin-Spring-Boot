package com.kazemieh.shop.customer.address.api.mapper

import com.kazemieh.shop.customer.address.api.dto.AddressResponse
import com.kazemieh.shop.customer.address.persistence.entity.AddressEntity

object AddressMapper {
    fun toResponse(a: AddressEntity) = AddressResponse(
        id = a.id,
        receiverName = a.receiverName,
        receiverPhone = a.receiverPhone,
        country = a.country,
        province = a.province,
        city = a.city,
        addressLine1 = a.addressLine1,
        addressLine2 = a.addressLine2,
        postalCode = a.postalCode,
        isDefault = a.isDefault,
        createdAt = a.createdAt
    )
}