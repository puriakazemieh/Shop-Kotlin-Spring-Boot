package com.kazemieh.shop.catalog.application

import com.kazemieh.shop.catalog.api.dto.PriceAlertResponse
import com.kazemieh.shop.catalog.application.exception.VariantNotFoundException
import com.kazemieh.shop.catalog.persistence.PriceAlertRepository
import com.kazemieh.shop.catalog.persistence.ProductVariantRepository
import com.kazemieh.shop.catalog.persistence.entity.PriceAlertEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class PriceAlertService(
    private val priceAlertRepository: PriceAlertRepository,
    private val variantRepository: ProductVariantRepository
) {
    private val log = LoggerFactory.getLogger(PriceAlertService::class.java)

    /** ثبتِ اشتراکِ «قیمت کم شد خبرم کن» برای کاربر روی یک واریانت با قیمتِ هدف. idempotent است. */
    @Transactional
    fun subscribe(userId: Long, productId: Long, variantId: Long, targetPrice: BigDecimal): PriceAlertResponse {
        if (!variantRepository.existsById(variantId)) throw VariantNotFoundException(variantId)

        val existing = priceAlertRepository.findByVariantIdAndUserId(variantId, userId)
        if (existing != null) {
            existing.targetPrice = targetPrice
            existing.notified = false
            existing.notifiedAt = null
            priceAlertRepository.save(existing)
        } else {
            priceAlertRepository.save(
                PriceAlertEntity(productId = productId, variantId = variantId, userId = userId, targetPrice = targetPrice)
            )
        }
        return PriceAlertResponse(variantId = variantId, targetPrice = targetPrice, subscribed = true)
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<PriceAlertResponse> =
        priceAlertRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map {
            PriceAlertResponse(variantId = it.variantId, targetPrice = it.targetPrice, subscribed = !it.notified)
        }

    /**
     * هنگامِ تغییرِ قیمتِ مؤثرِ یک واریانت (توسطِ ادمین) صدا زده می‌شود. اگر قیمتِ جدید به
     * قیمتِ هدفِ یکی از مشترکین رسیده یا کمتر باشد، اشتراکش را «مطلع‌شده» علامت می‌زند.
     */
    @Transactional
    fun onVariantPriceChanged(variantId: Long, newEffectivePrice: BigDecimal) {
        val eligible = priceAlertRepository.findAllByVariantIdAndNotifiedFalseAndTargetPriceGreaterThanEqual(variantId, newEffectivePrice)
        if (eligible.isEmpty()) return
        val now = OffsetDateTime.now()
        eligible.forEach {
            it.notified = true
            it.notifiedAt = now
        }
        priceAlertRepository.saveAll(eligible)
        log.info("Price dropped to {} for variant {} — marked {} price alerts", newEffectivePrice, variantId, eligible.size)
    }
}
