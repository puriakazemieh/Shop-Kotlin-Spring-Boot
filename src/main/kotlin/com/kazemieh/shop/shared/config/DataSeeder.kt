package com.kazemieh.shop.shared.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kazemieh.shop.blog.api.dto.BlogBlock
import com.kazemieh.shop.blog.persistence.BlogCategoryRepository
import com.kazemieh.shop.blog.persistence.BlogRepository
import com.kazemieh.shop.blog.persistence.entity.BlogCategoryEntity
import com.kazemieh.shop.blog.persistence.entity.BlogEntity
import com.kazemieh.shop.blog.persistence.entity.BlogStatus
import com.kazemieh.shop.catalog.persistence.BannerRepository
import com.kazemieh.shop.catalog.persistence.CampaignRepository
import com.kazemieh.shop.catalog.persistence.CategoryRepository
import com.kazemieh.shop.catalog.persistence.InventoryRepository
import com.kazemieh.shop.catalog.persistence.OptionTypeRepository
import com.kazemieh.shop.catalog.persistence.OptionValueRepository
import com.kazemieh.shop.catalog.persistence.ProductImageRepository
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.catalog.persistence.entity.BannerEntity
import com.kazemieh.shop.catalog.persistence.entity.CampaignEntity
import com.kazemieh.shop.catalog.persistence.entity.CategoryEntity
import com.kazemieh.shop.catalog.persistence.entity.InventoryEntity
import com.kazemieh.shop.catalog.persistence.entity.OptionTypeEntity
import com.kazemieh.shop.catalog.persistence.entity.OptionValueEntity
import com.kazemieh.shop.catalog.persistence.entity.ProductEntity
import com.kazemieh.shop.catalog.persistence.entity.ProductImageEntity
import com.kazemieh.shop.catalog.persistence.entity.ProductVariantEntity
import com.kazemieh.shop.customer.address.persistence.AddressRepository
import com.kazemieh.shop.customer.address.persistence.entity.AddressEntity
import com.kazemieh.shop.discount.persistence.DiscountRepository
import com.kazemieh.shop.discount.persistence.entity.DiscountEntity
import com.kazemieh.shop.discount.persistence.entity.DiscountType
import com.kazemieh.shop.identity.domain.UserRole
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.identity.persistence.entity.UserEntity
import com.kazemieh.shop.order.api.dto.AddressSnapshotResponse
import com.kazemieh.shop.order.persistence.OrderRepository
import com.kazemieh.shop.order.persistence.entity.OrderEntity
import com.kazemieh.shop.order.persistence.entity.OrderItemEntity
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.story.persistence.StoryRepository
import com.kazemieh.shop.story.persistence.entity.StoryEntity
import com.kazemieh.shop.story.persistence.entity.StoryMediaType
import com.kazemieh.shop.wallet.persistence.WalletRepository
import com.kazemieh.shop.wallet.persistence.WalletTransactionRepository
import com.kazemieh.shop.wallet.persistence.entity.TransactionType
import com.kazemieh.shop.wallet.persistence.entity.WalletEntity
import com.kazemieh.shop.wallet.persistence.entity.WalletTransactionEntity
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * داده‌ی نمونه‌ی جامع برای تستِ همه‌ی فیچرها (محصول/واریانت/تخفیف/کمپین/بنر/بلاگ/استوری/سفارش/کیف‌پول/آدرس).
 *
 * فقط وقتی اجرا می‌شود که `app.seed.enabled=true` باشد و جدول محصولات خالی باشد؛
 * بنابراین اجرای دوباره داده‌ی تکراری نمی‌سازد. برای ری‌ست، جداول را پاک کنید و دوباره اجرا کنید.
 *
 * حساب‌های ساخته‌شده:
 *   ادمین  → ایمیل admin@carmilla.test | موبایل 09120000000 | رمز admin1234
 *   مشتری  → ایمیل user@carmilla.test  | موبایل 09121111111 | رمز user1234
 */
@Component
@org.springframework.core.annotation.Order(1)
@ConditionalOnProperty(name = ["app.seed.enabled"], havingValue = "true")
class DataSeeder(
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val optionTypeRepository: OptionTypeRepository,
    private val optionValueRepository: OptionValueRepository,
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val inventoryRepository: InventoryRepository,
    private val campaignRepository: CampaignRepository,
    private val bannerRepository: BannerRepository,
    private val discountRepository: DiscountRepository,
    private val blogCategoryRepository: BlogCategoryRepository,
    private val blogRepository: BlogRepository,
    private val storyRepository: StoryRepository,
    private val addressRepository: AddressRepository,
    private val walletRepository: WalletRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val orderRepository: OrderRepository,
    private val passwordEncoder: PasswordEncoder,
    private val objectMapper: ObjectMapper,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(DataSeeder::class.java)

    @Transactional
    override fun run(vararg args: String) {
        if (productRepository.count() > 0L) {
            log.info("[DataSeeder] محصولات از قبل وجود دارند؛ از تولید داده‌ی نمونه صرف‌نظر شد.")
            return
        }
        log.info("[DataSeeder] شروع تولید داده‌ی نمونه‌ی کارمیلا…")

        val (admin, customer) = seedUsers()
        val categories = seedCategories()
        val (colors, sizes) = seedOptions()
        val products = seedProducts(categories, colors, sizes)
        seedCampaign(products)
        seedBanners(categories)
        seedDiscounts()
        seedBlog(admin)
        seedStories(products)
        val address = seedAddress(customer)
        seedWallet(customer)
        seedOrders(customer, products, address)

        log.info("[DataSeeder] پایان. ${products.size} محصول و داده‌ی کامل ساخته شد.")
    }

    private fun seedUsers(): Pair<UserEntity, UserEntity> {
        val admin = userRepository.save(
            UserEntity(
                email = "admin@carmilla.test",
                passwordHash = passwordEncoder.encode("admin1234")!!,
                firstName = "مدیر",
                lastName = "کارمیلا",
                city = "تهران",
                phone = "09120000000",
                role = UserRole.ADMIN,
                isActive = true,
            )
        )
        val customer = userRepository.save(
            UserEntity(
                email = "user@carmilla.test",
                passwordHash = passwordEncoder.encode("user1234")!!,
                firstName = "سارا",
                lastName = "محمدی",
                city = "تهران",
                phone = "09121111111",
                role = UserRole.CUSTOMER,
                isActive = true,
            )
        )
        return admin to customer
    }

    private fun seedCategories(): List<CategoryEntity> {
        val names = listOf(
            "مانتو" to "manto",
            "پالتو" to "palto",
            "بافت" to "baft",
            "شومیز" to "shomiz",
            "شلوار" to "shalvar",
            "اکسسوری" to "accessory",
        )
        return names.map { (name, slug) ->
            categoryRepository.save(CategoryEntity(name = name, slug = slug))
        }
    }

    /** برمی‌گرداند: (رنگ‌ها، سایزها) */
    private fun seedOptions(): Pair<List<OptionValueEntity>, List<OptionValueEntity>> {
        val colorType = optionTypeRepository.save(OptionTypeEntity(name = "رنگ"))
        val sizeType = optionTypeRepository.save(OptionTypeEntity(name = "سایز"))
        val colors = listOf("مشکی", "کرم", "زرشکی", "سرمه‌ای").map {
            optionValueRepository.save(OptionValueEntity(optionType = colorType, value = it))
        }
        val sizes = listOf("S", "M", "L", "XL").map {
            optionValueRepository.save(OptionValueEntity(optionType = sizeType, value = it))
        }
        return colors to sizes
    }

    private data class ProductSpec(
        val title: String,
        val slug: String,
        val categoryIndex: Int,
        val basePrice: Long,
        val discounted: Long?,
        val imageSeed: String,
    )

    private fun seedProducts(
        categories: List<CategoryEntity>,
        colors: List<OptionValueEntity>,
        sizes: List<OptionValueEntity>,
    ): List<ProductEntity> {
        val specs = listOf(
            ProductSpec("مانتو کتان مدل آرمیتا", "manto-armita", 0, 1_280_000, 980_000, "manto1"),
            ProductSpec("پالتو بلند زمستانی الیزه", "palto-elize", 1, 3_450_000, 2_760_000, "palto1"),
            ProductSpec("بافت یقه اسکی نیلوفر", "baft-niloofar", 2, 890_000, null, "baft1"),
            ProductSpec("شومیز ابریشمی رزا", "shomiz-roza", 3, 760_000, 599_000, "shomiz1"),
            ProductSpec("شلوار پارچه‌ای کلاسیک", "shalvar-classic", 4, 980_000, null, "shalvar1"),
            ProductSpec("مانتو مجلسی شب‌پره", "manto-shabpare", 0, 1_980_000, 1_490_000, "manto2"),
            ProductSpec("پالتو کتی مدل ویونا", "palto-viona", 1, 2_650_000, null, "palto2"),
            ProductSpec("بافت گیپوردار آوا", "baft-ava", 2, 1_120_000, 890_000, "baft2"),
            ProductSpec("شال نخی طرح پاییز", "shal-paeez", 5, 320_000, 249_000, "accessory1"),
            ProductSpec("کیف دستی چرم دلسا", "kif-delsa", 5, 1_450_000, null, "accessory2"),
        )

        return specs.map { spec ->
            val product = productRepository.save(
                ProductEntity(
                    category = categories[spec.categoryIndex],
                    title = spec.title,
                    slug = spec.slug,
                    description = "${spec.title} از مجموعه‌ی کارمیلا؛ دوختِ تمیز، پارچه‌ی مرغوب و مناسبِ استفاده‌ی روزمره و مجلسی. ارسال رایگان برای سفارش‌های بالای ۵۰۰ هزار تومان.",
                    basePrice = BigDecimal.valueOf(spec.basePrice),
                    discountedPrice = spec.discounted?.let { BigDecimal.valueOf(it) },
                    isActive = true,
                )
            )

            // تصاویر (۳ تصویر برای هر محصول)
            (0 until 3).forEach { i ->
                productImageRepository.save(
                    ProductImageEntity(
                        product = product,
                        url = "https://picsum.photos/seed/${spec.imageSeed}$i/700/860",
                        sortOrder = i,
                    )
                )
            }

            // واریانت‌ها: ۲ رنگ × ۳ سایز
            var skuIndex = 0
            colors.take(2).forEach { color ->
                sizes.take(3).forEach { size ->
                    val variant = productVariantRepository.save(
                        ProductVariantEntity(
                            product = product,
                            optionValues = mutableSetOf(color, size),
                            sku = "${spec.slug}-${skuIndex++}",
                            price = BigDecimal.valueOf(spec.basePrice),
                            discountedPrice = spec.discounted?.let { BigDecimal.valueOf(it) },
                            compareAtPrice = if (spec.discounted != null) BigDecimal.valueOf(spec.basePrice) else null,
                            isActive = true,
                        )
                    )
                    inventoryRepository.save(
                        InventoryEntity(variantId = variant.id, onHand = 15, reserved = 0)
                    )
                }
            }
            product
        }
    }

    private fun seedCampaign(products: List<ProductEntity>) {
        campaignRepository.save(
            CampaignEntity(
                title = "حراج پاییزی کارمیلا",
                endsAt = OffsetDateTime.now().plusDays(2).plusHours(6),
                isActive = true,
                products = products.filter { it.discountedPrice != null }.take(5).toMutableList(),
            )
        )
    }

    private fun seedBanners(categories: List<CategoryEntity>) {
        bannerRepository.save(
            BannerEntity(
                title = "کالکشن پاییز رسید",
                subtitle = "جدیدترین مانتوها",
                imageUrl = "https://picsum.photos/seed/banner1/900/400",
                categoryId = categories[0].id,
                sortOrder = 0,
                isActive = true,
            )
        )
        bannerRepository.save(
            BannerEntity(
                title = "تخفیف ویژه‌ی بافت",
                subtitle = "تا ۳۰٪ تخفیف",
                imageUrl = "https://picsum.photos/seed/banner2/900/400",
                categoryId = categories[2].id,
                sortOrder = 1,
                isActive = true,
            )
        )
    }

    private fun seedDiscounts() {
        discountRepository.save(
            DiscountEntity(
                code = "WELCOME10",
                type = DiscountType.PERCENTAGE,
                value = BigDecimal.TEN,
                maxDiscountAmount = BigDecimal.valueOf(200_000),
                minOrderAmount = BigDecimal.valueOf(500_000),
                startDate = OffsetDateTime.now().minusDays(5),
                endDate = OffsetDateTime.now().plusDays(30),
                usageLimit = 1000,
                usageCount = 12,
                isActive = true,
            )
        )
        discountRepository.save(
            DiscountEntity(
                code = "OFF50K",
                type = DiscountType.FIXED_AMOUNT,
                value = BigDecimal.valueOf(50_000),
                minOrderAmount = BigDecimal.valueOf(800_000),
                startDate = OffsetDateTime.now().minusDays(2),
                endDate = OffsetDateTime.now().plusDays(15),
                usageLimit = 500,
                usageCount = 3,
                isActive = true,
            )
        )
    }

    private fun seedBlog(admin: UserEntity) {
        val cat = blogCategoryRepository.save(
            BlogCategoryEntity(name = "راهنمای استایل", slug = "style-guide", description = "نکات پوشاک و ست کردن")
        )
        val cat2 = blogCategoryRepository.save(
            BlogCategoryEntity(name = "ترند روز", slug = "trends", description = "جدیدترین ترندهای مد")
        )
        val posts = listOf(
            Triple("چطور یک مانتوی پاییزی شیک انتخاب کنیم؟", "manto-paeezi-shik", cat),
            Triple("۵ ترکیب رنگی محبوب پاییز امسال", "tarkib-rangi-paeez", cat2),
            Triple("راهنمای نگهداری از بافت‌های زمستانی", "negahdari-baft", cat),
        )
        posts.forEachIndexed { i, (title, slug, c) ->
            blogRepository.save(
                BlogEntity(
                    title = title,
                    slug = slug,
                    content = listOf(
                        BlogBlock(type = "header", content = title, level = 1),
                        BlogBlock(
                            type = "paragraph",
                            content = "در این مقاله از مجلهٔ کارمیلا به بررسی نکات کاربردی می‌پردازیم. پوشاک مناسب فصل، انتخاب رنگ و دوختِ باکیفیت از مهم‌ترین عوامل یک استایل خوب هستند.",
                        ),
                        BlogBlock(type = "image", content = "https://picsum.photos/seed/blog$i/1000/560"),
                        BlogBlock(
                            type = "paragraph",
                            content = "با دنبال‌کردن این نکات می‌توانید ترکیبی شیک و کاربردی برای روزهای پاییزی بسازید.",
                        ),
                    ),
                    summary = "نکات کاربردی برای انتخاب و ست‌کردنِ پوشاکِ فصل از مجلهٔ کارمیلا.",
                    thumbnailUrl = "https://picsum.photos/seed/blogthumb$i/600/360",
                    viewCount = (120L * (i + 1)),
                    readingTimeMinutes = 4 + i,
                    status = BlogStatus.PUBLISHED,
                    authorId = admin.id,
                    categoryId = c.id,
                    isFeatured = i == 0,
                    metaTitle = title,
                    metaDescription = "نکات استایل کارمیلا",
                )
            )
        }
    }

    private fun seedStories(products: List<ProductEntity>) {
        val expiry = OffsetDateTime.now().plusDays(7)
        products.take(5).forEachIndexed { i, product ->
            storyRepository.save(
                StoryEntity(
                    mediaUrl = "https://picsum.photos/seed/story$i/720/1280",
                    mediaType = StoryMediaType.IMAGE,
                    productId = product.id,
                    title = product.title.take(20),
                    isActive = true,
                    expiresAt = expiry,
                )
            )
        }
    }

    private fun seedAddress(customer: UserEntity): AddressEntity {
        return addressRepository.save(
            AddressEntity(
                user = customer,
                receiverName = "سارا محمدی",
                receiverPhone = "09121111111",
                country = "IR",
                province = "تهران",
                city = "تهران",
                addressLine1 = "خیابان ولیعصر، کوچه‌ی بهار، پلاک ۱۲",
                addressLine2 = "واحد ۴",
                postalCode = "1234567890",
                isDefault = true,
            )
        )
    }

    private fun seedWallet(customer: UserEntity) {
        val wallet = walletRepository.save(
            WalletEntity(user = customer, balance = BigDecimal.valueOf(2_500_000))
        )
        walletTransactionRepository.save(
            WalletTransactionEntity(
                wallet = wallet,
                amount = BigDecimal.valueOf(3_000_000),
                type = TransactionType.DEPOSIT,
                description = "شارژ کیف پول از طریق درگاه",
            )
        )
        walletTransactionRepository.save(
            WalletTransactionEntity(
                wallet = wallet,
                amount = BigDecimal.valueOf(500_000),
                type = TransactionType.PURCHASE,
                description = "پرداخت بخشی از سفارش #۱",
                referenceId = "1",
            )
        )
    }

    private fun seedOrders(customer: UserEntity, products: List<ProductEntity>, address: AddressEntity) {
        val snapshot = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(
            AddressSnapshotResponse(
                receiverName = address.receiverName,
                receiverPhone = address.receiverPhone,
                country = address.country,
                province = address.province,
                city = address.city,
                addressLine1 = address.addressLine1,
                addressLine2 = address.addressLine2,
                postalCode = address.postalCode,
            )
        )

        // برای هر محصول، اولین واریانتش را برای آیتم سفارش پیدا می‌کنیم
        fun firstVariantOf(product: ProductEntity) =
            productVariantRepository.findAll().first { it.product?.id == product.id }

        data class OrderSpec(val status: OrderStatus, val productIdx: Int, val qty: Int, val shipped: Boolean)
        val orderSpecs = listOf(
            OrderSpec(OrderStatus.PLACED, 0, 1, false),
            OrderSpec(OrderStatus.PROCESSING, 2, 2, false),
            OrderSpec(OrderStatus.SHIPPING, 5, 1, true),
            OrderSpec(OrderStatus.COMPLETED, 8, 3, true),
        )

        orderSpecs.forEach { spec ->
            val product = products[spec.productIdx]
            val variant = firstVariantOf(product)
            val unit = variant.discountedPrice ?: variant.price
            val subtotal = unit.multiply(BigDecimal.valueOf(spec.qty.toLong()))
            val shipping = BigDecimal.valueOf(50_000)
            val total = subtotal.add(shipping)

            val order = OrderEntity(
                user = customer,
                status = spec.status,
                subtotalPrice = subtotal,
                shippingPrice = shipping,
                totalPrice = total,
                walletPaidAmount = BigDecimal.ZERO,
                gatewayPaidAmount = total,
                addressSnapshot = snapshot,
                shippingCarrier = if (spec.shipped) "پست پیشتاز" else null,
                trackingCode = if (spec.shipped) "IR${(100000..999999).random()}" else null,
                shippedAt = if (spec.shipped) OffsetDateTime.now().minusDays(1) else null,
                deliveredAt = if (spec.status == OrderStatus.COMPLETED) OffsetDateTime.now() else null,
            )
            order.items.add(
                OrderItemEntity(
                    order = order,
                    variantId = variant.id,
                    qty = spec.qty,
                    unitPriceSnapshot = unit,
                    titleSnapshot = product.title,
                    optionsSnapshot = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(
                        mapOf("رنگ" to "مشکی", "سایز" to "M")
                    ),
                )
            )
            orderRepository.save(order)
        }
    }
}
