package com.kazemieh.shop.catalog.application.exception

import com.kazemieh.shop.shared.error.ApiException
import org.springframework.http.HttpStatus

class ProductNotFoundException(slug: String) :
    ApiException("Product not found: $slug", "PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND)

class CategoryNotFoundException(slug: String) :
    ApiException("Category not found: $slug", "CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND)

class CategorySlugExistsException(slug: String) :
    ApiException("Category slug already exists: $slug", "CATEGORY_SLUG_EXISTS", HttpStatus.CONFLICT)

class ProductSlugExistsException(slug: String) :
    ApiException("Product slug already exists: $slug", "PRODUCT_SLUG_EXISTS", HttpStatus.CONFLICT)

class ImageNotFoundException(id: Long) :
    ApiException("Image not found: $id", "IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND)

class VariantNotFoundException(id: Long) :
    ApiException("Variant not found: $id", "VARIANT_NOT_FOUND", HttpStatus.NOT_FOUND)

class SkuExistsException(sku: String) :
    ApiException("SKU already exists: $sku", "SKU_EXISTS", HttpStatus.CONFLICT)

class InventoryConflictException :
    ApiException("Inventory version conflict", "INVENTORY_CONFLICT", HttpStatus.CONFLICT)

class BadRequestException(msg: String, code: String = "BAD_REQUEST") :
    ApiException(msg, code, HttpStatus.BAD_REQUEST)
