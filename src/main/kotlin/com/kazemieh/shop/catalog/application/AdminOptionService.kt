package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.application.exception.OptionTypeInUseException
import com.kazemieh.shop.catalog.application.exception.OptionTypeNotFoundException
import com.kazemieh.shop.catalog.application.exception.OptionValueInUseException
import com.kazemieh.shop.catalog.application.exception.OptionValueNotFoundException
import com.kazemieh.shop.catalog.persistence.OptionTypeRepository
import com.kazemieh.shop.catalog.persistence.OptionValueRepository
import com.kazemieh.shop.catalog.persistence.entity.OptionTypeEntity
import com.kazemieh.shop.catalog.persistence.entity.OptionValueEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminOptionService(
    private val optionTypeRepository: OptionTypeRepository,
    private val optionValueRepository: OptionValueRepository
) {

    @Transactional(readOnly = true)
    fun listAllOptions(): List<AdminOptionGroupResponse> {
        val types = optionTypeRepository.findAll()
        val values = optionValueRepository.findAll()

        val valuesByTypeId = values.groupBy { it.optionType.id }

        return types.map { type ->
            AdminOptionGroupResponse(
                id = type.id,
                name = type.name,
                values = valuesByTypeId[type.id]?.map { value ->
                    AdminOptionValueResponse(value.id, value.value)
                } ?: emptyList()
            )
        }
    }

    @Transactional
    fun createOptionType(req: AdminCreateOptionTypeRequest): AdminOptionTypeResponse {
        val saved = optionTypeRepository.save(OptionTypeEntity(name = req.name))
        return AdminOptionTypeResponse(saved.id, saved.name)
    }

    @Transactional
    fun updateOptionType(id: Long, req: AdminUpdateOptionTypeRequest): AdminOptionTypeResponse {
        val type = optionTypeRepository.findById(id).orElseThrow { OptionTypeNotFoundException(id) }
        type.name = req.name
        return AdminOptionTypeResponse(type.id, type.name)
    }

    @Transactional
    fun deleteOptionType(id: Long) {
        val type = optionTypeRepository.findById(id).orElseThrow { OptionTypeNotFoundException(id) }
        
        // Check if there are any values associated with this type
        val values = optionValueRepository.findAll().filter { it.optionType.id == id }
        if (values.isNotEmpty()) {
            throw OptionTypeInUseException(id)
        }

        optionTypeRepository.delete(type)
    }

    @Transactional
    fun createOptionValue(req: AdminCreateOptionValueRequest): AdminOptionValueResponse {
        val type = optionTypeRepository.findById(req.optionTypeId)
            .orElseThrow { OptionTypeNotFoundException(req.optionTypeId) }
        val saved = optionValueRepository.save(OptionValueEntity(optionType = type, value = req.value))
        return AdminOptionValueResponse(saved.id, saved.value)
    }

    @Transactional
    fun updateOptionValue(id: Long, req: AdminUpdateOptionValueRequest): AdminOptionValueResponse {
        val value = optionValueRepository.findById(id).orElseThrow { OptionValueNotFoundException(id) }
        value.value = req.value
        return AdminOptionValueResponse(value.id, value.value)
    }

    @Transactional
    fun deleteOptionValue(id: Long) {
        val optionValue = optionValueRepository.findById(id).orElseThrow { OptionValueNotFoundException(id) }

        if (optionValue.variants.isNotEmpty()) {
            throw OptionValueInUseException(id)
        }

        optionValueRepository.delete(optionValue)
    }
}