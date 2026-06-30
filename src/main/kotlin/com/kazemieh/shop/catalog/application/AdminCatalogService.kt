package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.api.mapper.AdminCatalogMapper
import com.kazemieh.shop.catalog.application.exception.*
import com.kazemieh.shop.catalog.persistence.*
import com.kazemieh.shop.catalog.persistence.entity.*
import com.kazemieh.shop.cart.persistence.CartItemRepository
import com.kazemieh.shop.order.persistence.OrderItemRepository
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AdminCatalogService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val imageRepository: ProductImageRepository,
    private val videoRepository: ProductVideoRepository,
    private val variantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val optionTypeRepository: OptionTypeRepository,
    private val optionValueRepository: OptionValueRepository,
    private val reviewRepository: ProductReviewRepository,
    private val questionRepository: ProductQuestionRepository,
    private val favoriteRepository: FavoriteRepository,
    private val cartItemRepository: CartItemRepository,
    private val orderItemRepository: OrderItemRepository
) {

    // ---------- Categories ----------
    @Transactional(readOnly = true)
    fun listCategories(): List<AdminCategoryResponse> =
        categoryRepository.findAll(Sort.by("name").ascending()).map(AdminCatalogMapper::category)

    @Transactional
    fun createCategory(req: AdminCreateCategoryRequest): AdminCategoryResponse {
        val slug = req.slug.trim()
        if (categoryRepository.existsBySlug(slug)) throw CategorySlugExistsException(slug)

        val parent = req.parentId?.let { pid ->
            categoryRepository.findById(pid).orElseThrow { CategoryNotFoundException(pid.toString()) }
        }

        val saved = categoryRepository.save(
            CategoryEntity(
                name = req.name.trim(),
                slug = slug,
                parent = parent
            )
        )
        return AdminCatalogMapper.category(saved)
    }

    @Transactional
    fun updateCategory(id: Long, req: AdminUpdateCategoryRequest): AdminCategoryResponse {
        val c = categoryRepository.findById(id).orElseThrow { CategoryNotFoundException(id.toString()) }

        req.name?.let { c.name = it.trim() }

        req.slug?.let {
            val newSlug = it.trim()
            if (newSlug != c.slug && categoryRepository.existsBySlug(newSlug)) {
                throw CategorySlugExistsException(newSlug)
            }
            c.slug = newSlug
        }

        if (req.parentId != null) {
            if (req.parentId == id) throw BadRequestException(
                "Category parent cannot be itself",
                "CATEGORY_PARENT_INVALID"
            )
            val parent =
                categoryRepository.findById(req.parentId)
                    .orElseThrow { CategoryNotFoundException(req.parentId.toString()) }
            c.parent = parent
        }

        return AdminCatalogMapper.category(c)
    }

    @Transactional
    fun deleteCategory(id: Long) {
        val c = categoryRepository.findById(id).orElseThrow { CategoryNotFoundException(id.toString()) }
        categoryRepository.delete(c)
    }

    // ---------- Products ----------
    @Transactional(readOnly = true)
    fun listProducts(page: Int, size: Int, includeInactive: Boolean): PageResponse<AdminProductResponse> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), Sort.by("createdAt").descending())
        val pageData =
            if (includeInactive) productRepository.findAll(pageable)
            else productRepository.findAll({ root, _, cb -> cb.isTrue(root.get("isActive")) }, pageable)

        return PageResponse(
            items = pageData.content.map(AdminCatalogMapper::product),
            page = pageData.number,
            size = pageData.size,
            totalElements = pageData.totalElements,
            totalPages = pageData.totalPages
        )
    }

    @Transactional
    fun createProduct(req: AdminCreateProductRequest): AdminProductResponse {
        val slug = req.slug.trim()
        if (productRepository.existsBySlug(slug)) throw ProductSlugExistsException(slug)

        val category = req.categoryId?.let { cid ->
            categoryRepository.findById(cid).orElseThrow { CategoryNotFoundException(cid.toString()) }
        }

        val saved = productRepository.save(
            ProductEntity(
                category = category,
                title = req.title.trim(),
                slug = slug,
                description = req.description,
                brand = req.brand?.trim()?.ifBlank { null },
                attributes = req.attributes
                    ?.filter { it.name.isNotBlank() }
                    ?.map { com.kazemieh.shop.catalog.persistence.entity.ProductAttribute(it.name.trim(), it.value.trim()) }
                    ?.toMutableList() ?: mutableListOf(),
                basePrice = req.basePrice,
                discountedPrice = req.discountedPrice,
                isActive = req.isActive
            )
        )

        if (req.variants.isNullOrEmpty()) {
            // Validate mandatory fields for default variant
            if (req.basePrice == null) {
                throw BadRequestException("Base price is required for products without variants", "PRODUCT_PRICE_REQUIRED")
            }
            if (req.initialOnHand == null) {
                throw BadRequestException("Initial inventory is required for products without variants", "PRODUCT_INVENTORY_REQUIRED")
            }

            // Create a default variant if none provided
            createDefaultVariant(saved, req.sku, req.initialOnHand)
        } else {
            req.variants.forEach { variantReq ->
                createVariant(saved.id, variantReq)
            }
        }

        return AdminCatalogMapper.product(saved)
    }

    private fun createDefaultVariant(product: ProductEntity, sku: String?, initialOnHand: Int) {
        createVariant(
            product.id, AdminCreateVariantRequest(
                options = emptyList(),
                sku = sku ?: "SKU-${product.id}",
                price = product.basePrice ?: BigDecimal.ZERO,
                discountedPrice = product.discountedPrice,
                isActive = product.isActive,
                initialOnHand = initialOnHand
            ),
            isSystem = true
        )
    }

    @Transactional
    fun updateProduct(id: Long, req: AdminUpdateProductRequest): AdminProductResponse {
        val p = productRepository.findById(id).orElseThrow { ProductNotFoundException(id.toString()) }

        if (req.categoryId != null) {
            p.category =
                categoryRepository.findById(req.categoryId)
                    .orElseThrow { CategoryNotFoundException(req.categoryId.toString()) }
        }

        req.title?.let { p.title = it.trim() }

        req.slug?.let {
            val newSlug = it.trim()
            if (newSlug != p.slug && productRepository.existsBySlug(newSlug)) throw ProductSlugExistsException(newSlug)
            p.slug = newSlug
        }

        if (req.description != null) p.description = req.description
        if (req.brand != null) p.brand = req.brand.trim().ifBlank { null }
        if (req.attributes != null) {
            p.attributes = req.attributes
                .filter { it.name.isNotBlank() }
                .map { com.kazemieh.shop.catalog.persistence.entity.ProductAttribute(it.name.trim(), it.value.trim()) }
                .toMutableList()
        }
        if (req.basePrice != null) p.basePrice = req.basePrice
        if (req.discountedPrice != null) p.discountedPrice = req.discountedPrice
        req.isActive?.let { p.isActive = it }

        val saved = productRepository.save(p)

        val currentVariants = variantRepository.findAllByProductId(id)
        if (currentVariants.isEmpty()) {
            // If no variants left, create a default one
            createDefaultVariant(saved, null, 0)
        } else if (currentVariants.size == 1 && currentVariants[0].optionValues.isEmpty()) {
            // If it's a simple product (only one variant with no options), sync its variant
            // User requested: Any change on price etc. should be applied to the default variant
            val v = currentVariants[0]
            req.basePrice?.let { v.price = it }
            req.discountedPrice?.let { v.discountedPrice = it }
            req.isActive?.let { v.isActive = it }
            variantRepository.save(v)
        }

        return AdminCatalogMapper.product(saved)
    }

    @Transactional(readOnly = true)
    fun productDetail(id: Long): AdminProductDetailResponse {
        val p = productRepository.findById(id).orElseThrow { ProductNotFoundException(id.toString()) }

        val images = imageRepository.findAllByProductIdOrderBySortOrderAsc(id)
        val videos = videoRepository.findAllByProductIdOrderBySortOrderAsc(id)
        val variants = variantRepository.findAllByProductId(id)
        
        // If there's only one variant and it's a default one (no options), 
        // we hide it from the variants list as per user request.
        val filteredVariants = if (variants.size == 1 && variants[0].optionValues.isEmpty()) {
            emptyList()
        } else {
            variants
        }
        
        val invMap = inventoryRepository.findAllById(variants.map { it.id }).associateBy { it.variantId }

        return AdminProductDetailResponse(
            product = AdminCatalogMapper.product(p),
            images = images.map(AdminCatalogMapper::image),
            videos = videos.map(AdminCatalogMapper::video),
            variants = filteredVariants.map { v -> AdminCatalogMapper.variant(v, invMap[v.id]) }
        )
    }

    @Transactional
    fun deleteProductHard(id: Long) {
        val p = productRepository.findById(id).orElseThrow { ProductNotFoundException(id.toString()) }
        
        // 1) Check if any variant is used in orders
        val variants = variantRepository.findAllByProductId(id)
        val variantIds = variants.map { it.id }
        
        for (vId in variantIds) {
            if (orderItemRepository.existsByVariantId(vId)) {
                throw BadRequestException(
                    "Cannot delete product because one of its variants is used in orders. Use soft delete (isActive=false) instead.",
                    "PRODUCT_USED_IN_ORDERS"
                )
            }
        }

        // 2) Manual cleanup of related data
        imageRepository.deleteAllByProductId(id)
        videoRepository.deleteAllByProductId(id)
        reviewRepository.deleteAllByProductId(id)
        questionRepository.deleteAllByProductId(id)
        favoriteRepository.deleteAllByProductId(id)
        
        for (vId in variantIds) {
            cartItemRepository.deleteAllByVariantId(vId)
            inventoryRepository.deleteById(vId)
        }
        
        variantRepository.deleteAll(variants)
        
        // 3) Delete product
        productRepository.delete(p)
    }

    // ---------- Images ----------
    @Transactional
    fun addImage(productId: Long, req: AdminAddImageRequest): AdminProductImageResponse {
        val p = productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        val sortOrder =
            req.sortOrder ?: ((imageRepository.findTopByProductIdOrderBySortOrderDesc(productId)?.sortOrder ?: -1) + 1)
        val saved = imageRepository.save(
            ProductImageEntity(
                product = p,
                url = req.url.trim(),
                sortOrder = sortOrder
            )
        )
        return AdminCatalogMapper.image(saved)
    }

    @Transactional
    fun reorderImages(productId: Long, req: AdminReorderImagesRequest): List<AdminProductImageResponse> {
        val p = productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        val current = imageRepository.findAllByProductIdOrderBySortOrderAsc(productId).associateBy { it.id }
        for (item in req.items) {
            val img = current[item.id] ?: throw ImageNotFoundException(item.id)
            if (img.product?.id != p.id) throw BadRequestException(
                "Image not belongs to product",
                "IMAGE_PRODUCT_MISMATCH"
            )
            img.sortOrder = item.sortOrder
        }
        // flush by transaction
        return imageRepository.findAllByProductIdOrderBySortOrderAsc(productId).map(AdminCatalogMapper::image)
    }

    @Transactional
    fun deleteImage(productId: Long, imageId: Long) {
        val img = imageRepository.findById(imageId).orElseThrow { ImageNotFoundException(imageId) }
        if (img.product?.id != productId) throw BadRequestException(
            "Image not belongs to product",
            "IMAGE_PRODUCT_MISMATCH"
        )
        imageRepository.delete(img)
    }

    // ---------- Videos ----------
    @Transactional
    fun addVideo(productId: Long, req: AdminAddVideoRequest): AdminProductVideoResponse {
        val p = productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        val sortOrder =
            req.sortOrder ?: ((videoRepository.findTopByProductIdOrderBySortOrderDesc(productId)?.sortOrder ?: -1) + 1)
        val saved = videoRepository.save(
            ProductVideoEntity(
                product = p,
                url = req.url.trim(),
                sortOrder = sortOrder
            )
        )
        return AdminCatalogMapper.video(saved)
    }

    @Transactional
    fun reorderVideos(productId: Long, req: AdminReorderVideosRequest): List<AdminProductVideoResponse> {
        val p = productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        val current = videoRepository.findAllByProductIdOrderBySortOrderAsc(productId).associateBy { it.id }
        for (item in req.items) {
            val vid = current[item.id] ?: throw VideoNotFoundException(item.id)
            if (vid.product?.id != p.id) throw BadRequestException(
                "Video not belongs to product",
                "VIDEO_PRODUCT_MISMATCH"
            )
            vid.sortOrder = item.sortOrder
        }
        return videoRepository.findAllByProductIdOrderBySortOrderAsc(productId).map(AdminCatalogMapper::video)
    }

    @Transactional
    fun deleteVideo(productId: Long, videoId: Long) {
        val vid = videoRepository.findById(videoId).orElseThrow { VideoNotFoundException(videoId) }
        if (vid.product?.id != productId) throw BadRequestException(
            "Video not belongs to product",
            "VIDEO_PRODUCT_MISMATCH"
        )
        videoRepository.delete(vid)
    }

    // ---------- Variants ----------
    @Transactional
    fun createVariant(productId: Long, req: AdminCreateVariantRequest, isSystem: Boolean = false): AdminVariantResponse {
        val product =
            productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        val variants = variantRepository.findAllByProductId(productId)

        if (!isSystem && req.options.isEmpty() && variants.isNotEmpty()) {
            throw BadRequestException("Variant must have at least one option", "VARIANT_OPTIONS_REQUIRED")
        }

        // If we are adding a "real" variant (with options), and there's currently only a default variant, delete it
        if (req.options.isNotEmpty()) {
            val defaultVariant = variants.find { it.optionValues.isEmpty() }
            if (defaultVariant != null) {
                // User requested: if a new variant is created, delete the default one
                inventoryRepository.deleteById(defaultVariant.id)
                variantRepository.delete(defaultVariant)
            }
        }

        val optionValues = req.options.map { optionPair ->
            val optionType = optionTypeRepository.findByName(optionPair.type)
                .orElseGet { optionTypeRepository.save(OptionTypeEntity(name = optionPair.type)) }

            optionValueRepository.findByOptionTypeIdAndValue(optionType.id, optionPair.value)
                .orElseGet { optionValueRepository.save(OptionValueEntity(optionType = optionType, value = optionPair.value)) }
        }.toMutableSet()

        // Check if a variant with the exact same options already exists for this product
        val existingVariant = variantRepository.findAllByProductId(productId).find { it.optionValues == optionValues }
        if (existingVariant != null) {
            throw VariantAlreadyExistsException(productId, optionValues.map { "${it.optionType.name}:${it.value}" })
        }

        val sku = req.sku.trim()
        if (variantRepository.existsBySku(sku)) throw SkuExistsException(sku)

        val variant = ProductVariantEntity(
            product = product,
            sku = sku,
            price = req.price,
            discountedPrice = req.discountedPrice,
            compareAtPrice = req.compareAtPrice,
            isActive = req.isActive
        )
        
        optionValues.forEach { variant.addOptionValue(it) }

        val savedVariant = variantRepository.save(variant)

        // create inventory row
        val inv = inventoryRepository.save(
            InventoryEntity(
                variantId = savedVariant.id,
                onHand = req.initialOnHand,
                reserved = 0,
                version = 0
            )
        )

        return AdminCatalogMapper.variant(savedVariant, inv)
    }

    @Transactional
    fun updateVariant(variantId: Long, req: AdminUpdateVariantRequest): AdminVariantResponse {
        val v = variantRepository.findWithAllOptionsById(variantId).orElseThrow { VariantNotFoundException(variantId) }

        if (v.optionValues.isEmpty() && !req.options.isNullOrEmpty()) {
            // Converting default variant to a specific one
            val inv = inventoryRepository.findById(variantId).orElse(null)
            val currentOnHand = inv?.onHand ?: 0
            
            val productId = v.product!!.id
            
            // Delete old default variant
            inventoryRepository.deleteById(variantId)
            variantRepository.delete(v)
            
            // Create new specific variant
            return createVariant(productId, AdminCreateVariantRequest(
                options = req.options,
                sku = req.sku ?: "SKU-$productId-${System.currentTimeMillis()}",
                price = req.price ?: v.price,
                discountedPrice = req.discountedPrice ?: v.discountedPrice,
                isActive = req.isActive ?: v.isActive,
                initialOnHand = currentOnHand
            ))
        }

        req.options?.let { optionPairs ->
            val newOptionValues = optionPairs.map { optionPair ->
                val optionType = optionTypeRepository.findByName(optionPair.type)
                    .orElseGet { optionTypeRepository.save(OptionTypeEntity(name = optionPair.type)) }

                optionValueRepository.findByOptionTypeIdAndValue(optionType.id, optionPair.value)
                    .orElseGet { optionValueRepository.save(OptionValueEntity(optionType = optionType, value = optionPair.value)) }
            }.toMutableSet()

            // Check if a variant with the exact same options already exists for this product
            val existingVariant = variantRepository.findAllByProductId(v.product!!.id).find { it.id != variantId && it.optionValues == newOptionValues }
            if (existingVariant != null) {
                throw VariantAlreadyExistsException(v.product!!.id, newOptionValues.map { "${it.optionType.name}:${it.value}" })
            }

            // Remove old ones
            v.optionValues.toList().forEach { v.removeOptionValue(it) }
            // Add new ones
            newOptionValues.forEach { v.addOptionValue(it) }
        }

        req.sku?.let {
            val newSku = it.trim()
            if (newSku != v.sku && variantRepository.existsBySku(newSku)) throw SkuExistsException(newSku)
            v.sku = newSku
        }
        req.price?.let { v.price = it }
        req.discountedPrice?.let { v.discountedPrice = it }
        req.compareAtPrice?.let { v.compareAtPrice = it }
        req.isActive?.let { v.isActive = it }

        val inv = inventoryRepository.findById(variantId).orElse(null)

        return AdminCatalogMapper.variant(v, inv)
    }

    @Transactional
    fun deleteVariant(variantId: Long) {
        val variant = variantRepository.findById(variantId).orElseThrow { VariantNotFoundException(variantId) }
        
        // Remove associated inventory first due to foreign key constraint
        inventoryRepository.deleteById(variantId)
        variantRepository.delete(variant)
    }

    // ---------- Inventory ----------
    @Transactional(readOnly = true)
    fun getInventory(variantId: Long): AdminInventoryResponse {
        val inv = inventoryRepository.findById(variantId)
            .orElseThrow { BadRequestException("Inventory not found for variant: $variantId", "INVENTORY_NOT_FOUND") }
        return AdminCatalogMapper.inventory(inv)
    }

    @Transactional
    fun setInventory(variantId: Long, req: AdminInventorySetRequest): AdminInventoryResponse {
        // ensure variant exists
        if (!variantRepository.existsById(variantId)) throw VariantNotFoundException(variantId)

        val inv = inventoryRepository.findById(variantId).orElse(
            InventoryEntity(variantId = variantId, onHand = 0, reserved = 0, version = 0)
        )

        req.version?.let { expected ->
            if (inv.version != expected) throw InventoryConflictException()
        }

        inv.onHand = req.onHand
        val saved = inventoryRepository.save(inv)
        return AdminCatalogMapper.inventory(saved)
    }

    @Transactional
    fun adjustInventory(variantId: Long, req: AdminInventoryAdjustRequest): AdminInventoryResponse {
        if (!variantRepository.existsById(variantId)) throw VariantNotFoundException(variantId)

        val inv = inventoryRepository.findById(variantId)
            .orElseThrow { BadRequestException("Inventory not found for variant: $variantId", "INVENTORY_NOT_FOUND") }

        req.version?.let { expected ->
            if (inv.version != expected) throw InventoryConflictException()
        }

        val newOnHand = inv.onHand + req.delta
        if (newOnHand < inv.reserved) throw BadRequestException(
            "onHand cannot be less than reserved",
            "INVENTORY_INVALID"
        )
        if (newOnHand < 0) throw BadRequestException("onHand cannot be negative", "INVENTORY_INVALID")

        inv.onHand = newOnHand
        val saved = inventoryRepository.save(inv)
        return AdminCatalogMapper.inventory(saved)
    }
}
