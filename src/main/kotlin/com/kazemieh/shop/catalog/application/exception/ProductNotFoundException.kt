package com.kazemieh.shop.catalog.application.exception

import com.kazemieh.shop.shared.error.ApiException
import org.springframework.http.HttpStatus

class ProductNotFoundException(slug: String) :
    ApiException("Product not found: $slug", "PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND)

class CategoryNotFoundException(slug: String) :
    ApiException("Category not found: $slug", "CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND)