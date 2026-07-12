package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.AdminBundleResponse
import com.kazemieh.shop.catalog.api.dto.AdminCreateBundleRequest
import com.kazemieh.shop.catalog.api.dto.AdminUpdateBundleRequest
import com.kazemieh.shop.catalog.persistence.ProductBundleRepository
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.entity.ProductBundleEntity
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminBundleService(
    private val bundleRepository: ProductBundleRepository,
    private val productRepository: ProductRepository
) {

    @Transactional(readOnly = true)
    fun list(): List<AdminBundleResponse> = bundleRepository.findAll().map { it.toResponse() }

    @Transactional
    fun create(req: AdminCreateBundleRequest): Long {
        val slug = req.slug.trim()
        if (bundleRepository.existsBySlug(slug)) {
            throw ConflictException("Bundle slug exists", ErrorCodes.BUNDLE_SLUG_EXISTS)
        }
        if (!productRepository.existsById(req.productId)) {
            throw NotFoundException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND)
        }
        val bundle = ProductBundleEntity(
            title = req.title.trim(),
            slug = slug,
            description = req.description,
            productId = req.productId,
            memberProductIds = req.memberProductIds.toMutableList(),
            isActive = req.isActive
        )
        return bundleRepository.save(bundle).id
    }

    @Transactional
    fun update(id: Long, req: AdminUpdateBundleRequest) {
        val b = findBundle(id)
        req.title?.let { b.title = it.trim() }
        req.description?.let { b.description = it }
        req.memberProductIds?.let { b.memberProductIds = it.toMutableList() }
        req.isActive?.let { b.isActive = it }
        bundleRepository.save(b)
    }

    @Transactional
    fun delete(id: Long) {
        bundleRepository.delete(findBundle(id))
    }

    private fun findBundle(id: Long): ProductBundleEntity =
        bundleRepository.findById(id).orElseThrow { NotFoundException("Bundle not found", ErrorCodes.BUNDLE_NOT_FOUND) }

    private fun ProductBundleEntity.toResponse() = AdminBundleResponse(
        id = id, title = title, slug = slug, description = description,
        productId = productId, memberProductIds = memberProductIds, isActive = isActive
    )
}
