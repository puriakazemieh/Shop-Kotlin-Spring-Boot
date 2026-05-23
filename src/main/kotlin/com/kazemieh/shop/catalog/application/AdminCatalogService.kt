package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.*
import com.kazemieh.shop.catalog.api.mapper.AdminCatalogMapper
import com.kazemieh.shop.catalog.application.exception.*
import com.kazemieh.shop.catalog.persistence.*
import com.kazemieh.shop.catalog.persistence.entity.*
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCatalogService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val imageRepository: ProductImageRepository,
    private val variantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val optionTypeRepository: OptionTypeRepository,
    private val optionValueRepository: OptionValueRepository
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
                basePrice = req.basePrice,
                isActive = req.isActive
            )
        )
        
        req.variants?.forEach { variantReq ->
            createVariant(saved.id, variantReq)
        }

        return AdminCatalogMapper.product(saved)
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
        if (req.basePrice != null) p.basePrice = req.basePrice
        req.isActive?.let { p.isActive = it }

        return AdminCatalogMapper.product(p)
    }

    @Transactional(readOnly = true)
    fun productDetail(id: Long): AdminProductDetailResponse {
        val p = productRepository.findById(id).orElseThrow { ProductNotFoundException(id.toString()) }

        val images = imageRepository.findAllByProductIdOrderBySortOrderAsc(id)
        val variants = variantRepository.findAllByProductId(id)
        val invMap = inventoryRepository.findAllById(variants.map { it.id }).associateBy { it.variantId }

        return AdminProductDetailResponse(
            product = AdminCatalogMapper.product(p),
            images = images.map(AdminCatalogMapper::image),
            variants = variants.map { v -> AdminCatalogMapper.variant(v, invMap[v.id]) }
        )
    }

    @Transactional
    fun deleteProductHard(id: Long) {
        // هشدار: اگر order_items به variant مربوط باشد، DB به خاطر ON DELETE RESTRICT اجازه نمی‌دهد.
        val p = productRepository.findById(id).orElseThrow { ProductNotFoundException(id.toString()) }
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

    // ---------- Variants ----------
    @Transactional
    fun createVariant(productId: Long, req: AdminCreateVariantRequest): AdminVariantResponse {
        val product =
            productRepository.findById(productId).orElseThrow { ProductNotFoundException(productId.toString()) }

        val optionValues = req.options.map { (optionTypeName, optionValueName) ->
            val optionType = optionTypeRepository.findByName(optionTypeName)
                .orElseGet { optionTypeRepository.save(OptionTypeEntity(name = optionTypeName)) }

            optionValueRepository.findByOptionTypeIdAndValue(optionType.id, optionValueName)
                .orElseGet { optionValueRepository.save(OptionValueEntity(optionType = optionType, value = optionValueName)) }
        }.toMutableSet()

        val sku = req.sku.trim()
        if (variantRepository.existsBySku(sku)) throw SkuExistsException(sku)

        val variant = variantRepository.save(
            ProductVariantEntity(
                product = product,
                optionValues = optionValues,
                sku = sku,
                price = req.price,
                compareAtPrice = req.compareAtPrice,
                isActive = req.isActive
            )
        )

        // create inventory row
        val inv = inventoryRepository.save(
            InventoryEntity(
                variantId = variant.id,
                onHand = req.initialOnHand,
                reserved = 0,
                version = 0
            )
        )

        return AdminCatalogMapper.variant(variant, inv)
    }

    @Transactional
    fun updateVariant(variantId: Long, req: AdminUpdateVariantRequest): AdminVariantResponse {
        val v = variantRepository.findById(variantId).orElseThrow { VariantNotFoundException(variantId) }

        req.options?.let { options ->
            v.optionValues.clear()
            val newOptionValues = options.map { (optionTypeName, optionValueName) ->
                val optionType = optionTypeRepository.findByName(optionTypeName)
                    .orElseGet { optionTypeRepository.save(OptionTypeEntity(name = optionTypeName)) }

                optionValueRepository.findByOptionTypeIdAndValue(optionType.id, optionValueName)
                    .orElseGet { optionValueRepository.save(OptionValueEntity(optionType = optionType, value = optionValueName)) }
            }.toMutableSet()
            v.optionValues.addAll(newOptionValues)
        }

        req.sku?.let {
            val newSku = it.trim()
            if (newSku != v.sku && variantRepository.existsBySku(newSku)) throw SkuExistsException(newSku)
            v.sku = newSku
        }
        req.price?.let { v.price = it }

        req.compareAtPrice?.let { v.compareAtPrice = it }
        req.isActive?.let { v.isActive = it }

        variantRepository.saveAndFlush(v)

        val inv = inventoryRepository.findById(variantId).orElse(null)

        // Eagerly fetch optionValues for the response
        val updatedVariant = variantRepository.findById(variantId).get()

        return AdminCatalogMapper.variant(updatedVariant, inv)
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
