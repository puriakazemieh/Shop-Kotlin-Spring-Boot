package com.kazemieh.shop.catalog.application.exception

import com.kazemieh.shop.shared.error.ApiException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.http.HttpStatus

class OptionTypeNotFoundException(id: Long) : NotFoundException("Option type not found: $id", ErrorCodes.OPTION_TYPE_NOT_FOUND)
class OptionValueNotFoundException(id: Long) : NotFoundException("Option value not found: $id", ErrorCodes.OPTION_VALUE_NOT_FOUND)

class OptionValueInUseException(id: Long) :
    ApiException("Option value with id $id is currently in use by a product variant and cannot be deleted.", "OPTION_VALUE_IN_USE", HttpStatus.CONFLICT)

class OptionTypeInUseException(id: Long) :
    ApiException("Option type with id $id has values and cannot be deleted.", "OPTION_TYPE_IN_USE", HttpStatus.CONFLICT)
