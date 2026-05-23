package com.kazemieh.shop.shared.error

import jakarta.servlet.http.HttpServletRequest
import org.postgresql.util.PSQLException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.hibernate.exception.ConstraintViolationException as HibernateConstraintViolationException

@RestControllerAdvice
class GlobalExceptionHandler {

    private fun build(
        status: HttpStatus,
        message: String,
        errorCode: String?,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val body = ApiError(
            status = status,
            message = message,
            errorCode = errorCode,
            path = request.requestURI
        )
        return ResponseEntity(body, status)
    }

    // ===== Your own exceptions =====
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException, request: HttpServletRequest): ResponseEntity<ApiError> {
        return build(ex.httpStatus, ex.message ?: "Error", ex.errorCode, request)
    }

    // If you use Spring Security
    @ExceptionHandler(AccessDeniedException::class)
    fun handleSpringAccessDenied(ex: AccessDeniedException, request: HttpServletRequest): ResponseEntity<ApiError> {
        return build(HttpStatus.FORBIDDEN, ex.message ?: "Access denied", ErrorCodes.ACCESS_DENIED, request)
    }

    // Bean validation (if using @Valid)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val msg = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { "Validation error" }

        return build(HttpStatus.BAD_REQUEST, msg, "VALIDATION_ERROR", request)
    }

    // ===== Database integrity translation (UNIQUE / FK / CHECK) =====
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        ex.printStackTrace() // Temporarily add this for debugging
        val translated = translateDataIntegrity(ex)

        return build(
            translated.status,
            translated.message,
            translated.errorCode,
            request
        )
    }

    // Fallback
    @ExceptionHandler(Exception::class)
    fun handleAny(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {

        ex.printStackTrace()

        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            ErrorCodes.INTERNAL_ERROR,
            request
        )
    }
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleBadJson(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {

        return build(
            HttpStatus.BAD_REQUEST,
            "Malformed JSON request",
            "INVALID_JSON",
            request
        )
    }
    // ===== Helpers =====

    private data class TranslatedDbError(
        val status: HttpStatus,
        val message: String,
        val errorCode: String
    )

    private fun translateDataIntegrity(ex: DataIntegrityViolationException): TranslatedDbError {
        val constraint = extractConstraintName(ex)
        val sqlState = extractSqlState(ex)

        // 23505 unique_violation
        // 23503 foreign_key_violation
        // 23514 check_violation
        // Postgres SQLSTATE ref: typical codes, we map without depending on exact message text.

        // Map by constraint name first (best)
        if (!constraint.isNullOrBlank()) {
            return when (constraint) {
                // ===== UNIQUE constraints =====
                "users_email_key" ->
                    TranslatedDbError(HttpStatus.CONFLICT, "Email already exists", ErrorCodes.EMAIL_ALREADY_EXISTS)

                "categories_slug_key" ->
                    TranslatedDbError(
                        HttpStatus.CONFLICT,
                        "Category slug already exists",
                        ErrorCodes.CATEGORY_SLUG_EXISTS
                    )

                "products_slug_key" ->
                    TranslatedDbError(
                        HttpStatus.CONFLICT,
                        "Product slug already exists",
                        ErrorCodes.PRODUCT_SLUG_EXISTS
                    )

                "option_type_name_key" ->
                    TranslatedDbError(HttpStatus.CONFLICT, "Option type already exists", ErrorCodes.SIZE_EXISTS)

                "option_value_option_type_id_value_key" ->
                    TranslatedDbError(HttpStatus.CONFLICT, "Option value already exists", ErrorCodes.COLOR_EXISTS)

                "product_variants_sku_key" ->
                    TranslatedDbError(HttpStatus.CONFLICT, "SKU already exists", ErrorCodes.SKU_EXISTS)

                "uq_variant_unique_combo" ->
                    TranslatedDbError(
                        HttpStatus.CONFLICT,
                        "Variant combination already exists",
                        ErrorCodes.VARIANT_COMBO_EXISTS
                    )

                // ===== CHECK constraints =====
                "chk_prices_nonneg" ->
                    TranslatedDbError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Invalid variant prices",
                        ErrorCodes.INVALID_VARIANT_PRICE
                    )

                "chk_inventory_nonneg" ->
                    TranslatedDbError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Inventory values cannot be negative",
                        ErrorCodes.INVALID_INVENTORY
                    )

                "chk_reserved_le_onhand" ->
                    TranslatedDbError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Reserved cannot be greater than on_hand",
                        ErrorCodes.INVALID_INVENTORY
                    )

                "chk_order_prices_nonneg" ->
                    TranslatedDbError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Order prices cannot be negative",
                        ErrorCodes.CHECK_VIOLATION
                    )

                "chk_order_item_qty" ->
                    TranslatedDbError(HttpStatus.BAD_REQUEST, "Quantity must be > 0", ErrorCodes.INVALID_QUANTITY)

                "chk_order_item_price" ->
                    TranslatedDbError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Order item price cannot be negative",
                        ErrorCodes.INVALID_ORDER_ITEM_PRICE
                    )

                // ===== FK constraints (bad input references) =====
                // These names are default Postgres-generated. If you named constraints differently, adjust here.
                "products_category_id_fkey",
                "categories_parent_id_fkey",
                "product_images_product_id_fkey",
                "product_variants_product_id_fkey",
                "addresses_user_id_fkey",
                "orders_user_id_fkey",
                "order_items_order_id_fkey",
                "order_items_variant_id_fkey" ->
                    TranslatedDbError(
                        HttpStatus.BAD_REQUEST,
                        "Invalid reference (foreign key)",
                        ErrorCodes.FOREIGN_KEY_VIOLATION
                    )

                else -> {
                    // unknown constraint name
                    val fallbackCode = when (sqlState) {
                        "23505" -> ErrorCodes.UNIQUE_VIOLATION
                        "23503" -> ErrorCodes.FOREIGN_KEY_VIOLATION
                        "23514" -> ErrorCodes.CHECK_VIOLATION
                        else -> ErrorCodes.DATA_INTEGRITY_VIOLATION
                    }
                    val status = when (sqlState) {
                        "23505" -> HttpStatus.CONFLICT
                        "23503" -> HttpStatus.BAD_REQUEST
                        "23514" -> HttpStatus.UNPROCESSABLE_ENTITY
                        else -> HttpStatus.CONFLICT
                    }
                    TranslatedDbError(status, "Data integrity violation", fallbackCode)
                }
            }
        }

        // If no constraint name found, fall back to SQLSTATE
        return when (sqlState) {
            "23505" -> TranslatedDbError(HttpStatus.CONFLICT, "Duplicate value", ErrorCodes.UNIQUE_VIOLATION)
            "23503" -> TranslatedDbError(
                HttpStatus.BAD_REQUEST,
                "Invalid reference (foreign key)",
                ErrorCodes.FOREIGN_KEY_VIOLATION
            )

            "23514" -> TranslatedDbError(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid data (check constraint)",
                ErrorCodes.CHECK_VIOLATION
            )

            else -> TranslatedDbError(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                ErrorCodes.DATA_INTEGRITY_VIOLATION
            )
        }
    }

    private fun extractConstraintName(t: Throwable): String? {
        // Hibernate wraps it:
        // DataIntegrityViolationException -> HibernateConstraintViolationException -> constraintName
        var cur: Throwable? = t
        while (cur != null) {
            if (cur is HibernateConstraintViolationException) {
                return cur.constraintName
            }
            cur = cur.cause
        }

        // Sometimes PSQLException carries constraint in message only, but try to avoid parsing message.
        // We'll still attempt to detect via server error message if available:
        cur = t
        while (cur != null) {
            if (cur is PSQLException) {
                return cur.serverErrorMessage?.constraint
            }
            cur = cur.cause
        }
        return null
    }

    private fun extractSqlState(t: Throwable): String? {
        var cur: Throwable? = t
        while (cur != null) {
            if (cur is PSQLException) return cur.sqlState
            cur = cur.cause
        }
        return null
    }
}
