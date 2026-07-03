package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.persistence.SessionCreditRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.SessionCreditEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * اعطای اعتبارِ جلسه پس از خریدِ محصولِ متناظر.
 *
 * پلِ بینِ ماژولِ سفارش و کلینیک: وقتی سفارشی پرداخت می‌شود (وضعیت PROCESSING)،
 * OrderService این سرویس را با نگاشتِ (شناسه‌ی محصول → تعداد) سفارش صدا می‌زند و برای
 * درمانگرهایی که به آن محصولات لینک شده‌اند (Therapist.productId)، به تعدادِ خریداری‌شده
 * اعتبارِ جلسه به کاربر اضافه می‌کند.
 */
@Service
class ClinicAccessService(
    private val therapistRepository: TherapistRepository,
    private val creditRepository: SessionCreditRepository
) {

    @Transactional
    fun grantSessionCredits(userId: Long, productQuantities: Map<Long, Int>) {
        if (productQuantities.isEmpty()) return
        val therapists = therapistRepository.findAllByProductIdIn(productQuantities.keys)
        for (therapist in therapists) {
            val qty = productQuantities[therapist.productId] ?: continue
            if (qty <= 0) continue
            val credit = creditRepository.findByUserIdAndTherapistIdForUpdate(userId, therapist.id)
                ?: SessionCreditEntity(userId = userId, therapistId = therapist.id, remaining = 0)
            credit.remaining += qty
            creditRepository.save(credit)
        }
    }
}
