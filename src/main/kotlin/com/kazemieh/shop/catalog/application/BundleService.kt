package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.BundleDetailResponse
import com.kazemieh.shop.catalog.api.dto.BundleSummaryResponse
import com.kazemieh.shop.catalog.persistence.ProductBundleRepository
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * باندل/پکیجِ محصول — عمومی (کاتالوگ). خودِ باندل یک محصولِ واقعیِ قابلِ‌خرید است؛
 * این سرویس فقط اطلاعاتِ نمایشی (محصولِ اصلی + اعضا) را جمع می‌کند، بدونِ دخالت در سبد/سفارش.
 */
@Service
class BundleService(
    private val bundleRepository: ProductBundleRepository,
    private val catalogService: CatalogService
) {

    @Transactional(readOnly = true)
    fun list(userId: Long? = null): List<BundleSummaryResponse> {
        val bundles = bundleRepository.findAllByIsActiveTrueOrderByCreatedAtDesc()
        if (bundles.isEmpty()) return emptyList()
        val productById = catalogService.summarizeProducts(bundles.map { it.productId }, userId).associateBy { it.id }
        return bundles.mapNotNull { b ->
            val product = productById[b.productId] ?: return@mapNotNull null
            BundleSummaryResponse(
                id = b.id, title = b.title, slug = b.slug, description = b.description,
                product = product, memberCount = b.memberProductIds.size
            )
        }
    }

    @Transactional(readOnly = true)
    fun detail(slug: String, userId: Long? = null): BundleDetailResponse {
        val bundle = bundleRepository.findBySlug(slug)
            ?: throw NotFoundException("Bundle not found", ErrorCodes.BUNDLE_NOT_FOUND)
        val allIds = (listOf(bundle.productId) + bundle.memberProductIds).distinct()
        val summariesById = catalogService.summarizeProducts(allIds, userId).associateBy { it.id }
        val product = summariesById[bundle.productId]
            ?: throw NotFoundException("Bundle product not found", ErrorCodes.PRODUCT_NOT_FOUND)
        val members = bundle.memberProductIds.mapNotNull { summariesById[it] }
        return BundleDetailResponse(
            id = bundle.id, title = bundle.title, slug = bundle.slug,
            description = bundle.description, product = product, members = members
        )
    }
}
