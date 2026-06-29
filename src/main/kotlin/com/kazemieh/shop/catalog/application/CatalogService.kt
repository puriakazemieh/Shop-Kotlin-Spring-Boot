package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.api.mapper.CatalogMapper
import com.kazemieh.shop.catalog.application.exception.ProductNotFoundException
import com.kazemieh.shop.catalog.persistence.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CatalogService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val productVideoRepository: ProductVideoRepository,
    private val variantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val productSearchRepository: ProductSearchRepository,
    private val favoriteRepository: FavoriteRepository,
    private val productReviewRepository: ProductReviewRepository,
) {

    // ---------- Categories (Tree) ----------
    @Transactional(readOnly = true)
    fun categoriesTree(): List<CategoryResponse> {
        val all = categoryRepository.findAll()
        val byParent = all.groupBy { it.parent?.id }
        fun build(parentId: Long?): List<CategoryResponse> =
            (byParent[parentId] ?: emptyList())
                .sortedBy { it.name.lowercase() }
                .map { c ->
                    CategoryResponse(
                        id = c.id,
                        name = c.name,
                        slug = c.slug,
                        parentId = c.parent?.id,
                        children = build(c.id)
                    )
                }
        return build(null)
    }

    // ---------- Products list ----------
    @Transactional(readOnly = true)
    fun listProducts(
        q: String?,
        categoryId: Long?,
        options: Map<String, String>?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        categorySlug: String?,
        sort: String?,
        currentUserId: Long? = null
    ): PageResponse<ProductSummaryResponse> {
        val resolvedCategoryId = when {
            categoryId != null -> categoryId
            !categorySlug.isNullOrBlank() -> categoryRepository.findBySlug(categorySlug.trim())?.id
            else -> null
        }
        val needVariantFilter =
            !options.isNullOrEmpty() || minPrice != null || maxPrice != null || (inStock == true)

        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.unsorted()
        )

        val qNorm = q?.trim().orEmpty()

        val sortKey = sort?.trim()?.lowercase()

        val pageData = when {
            qNorm.isNotBlank() -> {
                // TODO: Update search to support dynamic options
                val res = productSearchRepository.searchRelevance(
                    q = qNorm,
                    categoryId = resolvedCategoryId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    inStock = inStock,
                    needVariantFilter = needVariantFilter,
                    pageable = pageable
                )
                if (res.isEmpty && qNorm.length >= 3) {
                    productSearchRepository.searchFuzzyTitle(qNorm, resolvedCategoryId, pageable)
                } else res
            }
            else -> when (sortKey) {
                "price_asc" -> productSearchRepository.searchPriceAsc(
                    categoryId = resolvedCategoryId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    inStock = inStock,
                    needVariantFilter = needVariantFilter,
                    pageable = pageable
                )
                "price_desc" -> productSearchRepository.searchPriceDesc(
                    categoryId = resolvedCategoryId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    inStock = inStock,
                    needVariantFilter = needVariantFilter,
                    pageable = pageable
                )
                else -> productSearchRepository.searchNewest(
                    categoryId = resolvedCategoryId,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    inStock = inStock,
                    needVariantFilter = needVariantFilter,
                    pageable = pageable
                )
            }
        }

        val products = pageData.content
        val productIds = products.map { it.id }

        val images = if (productIds.isNotEmpty())
            productImageRepository.findAllByProductIdInOrderBySortOrderAsc(productIds)
        else emptyList()

        val thumbnailByProductId = images
            .groupBy { it.product?.id ?: 0L }
            .mapValues { (_, list) -> list.firstOrNull()?.url }

        val aggByProductId = if (productIds.isNotEmpty())
            variantRepository.aggregateByProductIds(productIds).associateBy { it.getProductId() }
        else emptyMap()

        val favoriteProductIds = if (currentUserId != null && productIds.isNotEmpty()) {
            favoriteRepository.findAllByUserIdAndProductIdIn(currentUserId, productIds).map { it.product.id }.toSet()
        } else emptySet()

        val ratingByProductId = if (productIds.isNotEmpty())
            productReviewRepository.aggregateRatingsByProductIds(productIds).associateBy { it.getProductId() }
        else emptyMap()

        val items = products.map { p ->
            val agg = aggByProductId[p.id]
            val rating = ratingByProductId[p.id]
            ProductSummaryResponse(
                id = p.id,
                title = p.title,
                slug = p.slug,
                thumbnailUrl = thumbnailByProductId[p.id],
                minPrice = agg?.getMinPrice(),
                maxPrice = agg?.getMaxPrice(),
                minDiscountedPrice = agg?.getMinDiscountedPrice(),
                maxDiscountedPrice = agg?.getMaxDiscountedPrice(),
                inStock = agg?.getInStock() ?: false,
                categoryId = p.category?.id,
                categoryName = p.category?.name,
                isFavorite = favoriteProductIds.contains(p.id),
                averageRating = rating?.getAvgRating(),
                reviewCount = rating?.getReviewCount() ?: 0,
            )
        }

        return PageResponse(
            items = items,
            page = pageData.number,
            size = pageData.size,
            totalElements = pageData.totalElements,
            totalPages = pageData.totalPages
        )
    }

    // ---------- Product detail ----------
    @Transactional(readOnly = true)
    fun productDetail(slug: String, currentUserId: Long? = null): ProductDetailResponse {
        val p = productRepository.findBySlugAndIsActiveTrue(slug) ?: throw ProductNotFoundException(slug)

        val images = productImageRepository.findAllByProductIdOrderBySortOrderAsc(p.id)
        val videos = productVideoRepository.findAllByProductIdOrderBySortOrderAsc(p.id)
        val variants = variantRepository.findActiveWithOptions(p.id)

        val variantIds = variants.map { it.id }
        val inventory = if (variantIds.isNotEmpty()) inventoryRepository.findAllById(variantIds) else emptyList()
        val invByVariant = inventory.associateBy { it.variantId }

        val variantResponses = variants.map { v ->
            val inv = invByVariant[v.id]
            val available = ((inv?.onHand ?: 0) - (inv?.reserved ?: 0)).coerceAtLeast(0)
            CatalogMapper.toVariant(v, available)
        }

        val isFavorite = currentUserId?.let { favoriteRepository.existsByUserIdAndProductId(it, p.id) } ?: false

        return ProductDetailResponse(
            id = p.id,
            title = p.title,
            slug = p.slug,
            description = p.description,
            basePrice = p.basePrice,
            discountedPrice = p.discountedPrice,
            categoryId = p.category?.id,
            categoryName = p.category?.name,
            images = images.map(CatalogMapper::toImage),
            videos = videos.map(CatalogMapper::toVideo),
            variants = variantResponses,
            createdAt = p.createdAt,
            isFavorite = isFavorite
        )
    }
}
