package com.kazemieh.shop.customer.address.application

import com.kazemieh.shop.customer.address.api.dto.CreateAddressRequest
import com.kazemieh.shop.customer.address.api.dto.UpdateAddressRequest
import com.kazemieh.shop.customer.address.api.mapper.AddressMapper
import com.kazemieh.shop.customer.address.persistence.AddressRepository
import com.kazemieh.shop.customer.address.persistence.entity.AddressEntity
import com.kazemieh.shop.identity.application.exception.UserNotFoundException
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.shared.error.AddressAccessDeniedException
import com.kazemieh.shop.shared.error.AddressNotFoundException
import com.kazemieh.shop.shared.error.InvalidAddressException
import com.kazemieh.shop.shared.error.NotFoundException
import com.kazemieh.shop.shared.error.ErrorCodes
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddressService(
    private val addressRepository: AddressRepository,
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun list(userId: Long) =
        addressRepository.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
            .map(AddressMapper::toResponse)

    @Transactional(readOnly = true)
    fun get(userId: Long, addressId: Long) =
        AddressMapper.toResponse(findOwnedAddress(userId, addressId))

    @Transactional(readOnly = true)
    fun getDefault(userId: Long) =
        addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
            ?.let(AddressMapper::toResponse)
            ?: throw NotFoundException("Default address not found", ErrorCodes.ADDRESS_NOT_FOUND)

    @Transactional
    fun create(userId: Long, req: CreateAddressRequest) : com.kazemieh.shop.customer.address.api.dto.AddressResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        val hasAny = addressRepository.countByUserId(userId) > 0
        val makeDefault = req.setAsDefault || !hasAny

        if (makeDefault) {
            addressRepository.clearDefaultForUser(userId)
        }

        val entity = AddressEntity(
            user = user,
            receiverName = req.receiverName.trim(),
            receiverPhone = req.receiverPhone.trim(),
            country = req.country.trim().uppercase(),
            province = req.province.trim(),
            city = req.city.trim(),
            addressLine1 = req.addressLine1.trim(),
            addressLine2 = req.addressLine2?.trim(),
            postalCode = req.postalCode?.trim(),
            isDefault = makeDefault
        )

        val saved = addressRepository.save(entity)
        return AddressMapper.toResponse(saved)
    }

    @Transactional
    fun update(userId: Long, addressId: Long, req: UpdateAddressRequest) : com.kazemieh.shop.customer.address.api.dto.AddressResponse {
        val a = findOwnedAddress(userId, addressId)

        val nothing =
            req.receiverName == null &&
            req.receiverPhone == null &&
            req.country == null &&
            req.province == null &&
            req.city == null &&
            req.addressLine1 == null &&
            req.addressLine2 == null &&
            req.postalCode == null

        if (nothing) throw InvalidAddressException("No fields to update")

        req.receiverName?.let { a.receiverName = it.trim() }
        req.receiverPhone?.let { a.receiverPhone = it.trim() }
        req.country?.let { a.country = it.trim().uppercase() }
        req.province?.let { a.province = it.trim() }
        req.city?.let { a.city = it.trim() }
        req.addressLine1?.let { a.addressLine1 = it.trim() }
        req.addressLine2?.let { a.addressLine2 = it.trim() }
        req.postalCode?.let { a.postalCode = it.trim() }

        return AddressMapper.toResponse(a)
    }

    @Transactional
    fun delete(userId: Long, addressId: Long) {
        val a = findOwnedAddress(userId, addressId)
        val wasDefault = a.isDefault

        addressRepository.delete(a)

        // اگر default حذف شد، یکی دیگه را default کن
        if (wasDefault) {
            val replacement = addressRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
            if (replacement != null) {
                addressRepository.clearDefaultForUser(userId)
                replacement.isDefault = true
            }
        }
    }

    @Transactional
    fun setDefault(userId: Long, addressId: Long) : com.kazemieh.shop.customer.address.api.dto.AddressResponse {
        val a = findOwnedAddress(userId, addressId)
        addressRepository.clearDefaultExcept(userId, a.id)
        a.isDefault = true
        return AddressMapper.toResponse(a)
    }

    private fun findOwnedAddress(userId: Long, addressId: Long): AddressEntity {
        val a = addressRepository.findById(addressId).orElseThrow { AddressNotFoundException(addressId) }
        val ownerId = a.user?.id ?: 0L
        if (ownerId != userId) throw AddressAccessDeniedException(addressId)
        return a
    }
}