package com.kazemieh.shop.catalog.application.exception

import com.kazemieh.shop.shared.error.NotFoundException
import com.kazemieh.shop.shared.error.ErrorCodes

class OptionTypeNotFoundException(id: Long) : NotFoundException("Option type not found: $id", ErrorCodes.OPTION_TYPE_NOT_FOUND)
class OptionValueNotFoundException(id: Long) : NotFoundException("Option value not found: $id", ErrorCodes.OPTION_VALUE_NOT_FOUND)
