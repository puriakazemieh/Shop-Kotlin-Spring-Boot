package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.StockNotificationResponse
import com.kazemieh.shop.catalog.application.exception.VariantNotFoundException
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.catalog.persistence.StockNotificationRepository
import com.kazemieh.shop.catalog.persistence.entity.StockNotificationEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class StockNotificationService(
    private val stockNotificationRepository: StockNotificationRepository,
    private val variantRepository: ProductVariantRepository
) {
    private val log = LoggerFactory.getLogger(StockNotificationService::class.java)

    /** ثبتِ اشتراکِ «موجود شد خبرم کن» برای کاربر روی یک واریانت. idempotent است. */
    @Transactional
    fun subscribe(userId: Long, productId: Long, variantId: Long): StockNotificationResponse {
        if (!variantRepository.existsById(variantId)) throw VariantNotFoundException(variantId)

        val existing = stockNotificationRepository.findByVariantIdAndUserId(variantId, userId)
        if (existing != null) {
            // اگر قبلاً مطلع شده بود، دوباره فعال کن.
            existing.notified = false
            existing.notifiedAt = null
            stockNotificationRepository.save(existing)
        } else {
            stockNotificationRepository.save(
                StockNotificationEntity(productId = productId, variantId = variantId, userId = userId)
            )
        }
        return StockNotificationResponse(variantId = variantId, subscribed = true)
    }

    /**
     * هنگامِ شارژِ دوباره‌ی موجودیِ یک واریانت (گذر از ناموجود به موجود) صدا زده می‌شود.
     * فعلاً فقط اشتراک‌های در انتظار را علامتِ «مطلع‌شده» می‌زند؛ ارسالِ واقعیِ SMS/ایمیل
     * می‌تواند بعداً به این نقطه اضافه شود.
     */
    @Transactional
    fun onVariantRestocked(variantId: Long) {
        val pending = stockNotificationRepository.findAllByVariantIdAndNotifiedFalse(variantId)
        if (pending.isEmpty()) return
        val now = OffsetDateTime.now()
        pending.forEach {
            it.notified = true
            it.notifiedAt = now
        }
        stockNotificationRepository.saveAll(pending)
        log.info("Stock replenished for variant {} — marked {} back-in-stock notifications", variantId, pending.size)
    }
}
