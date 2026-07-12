package com.kazemieh.shop.clinic.application

import com.kazemieh.shop.clinic.persistence.MessagingPlanRepository
import com.kazemieh.shop.clinic.persistence.SessionCreditRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.clinic.persistence.entity.MessagingPlanEntity
import com.kazemieh.shop.clinic.persistence.entity.SessionCreditEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * اعطای اعتبارِ جلسه/پلنِ پیام‌رسانیِ نامحدود پس از خریدِ محصولِ متناظر.
 *
 * پلِ بینِ ماژولِ سفارش و کلینیک: وقتی سفارشی پرداخت می‌شود (وضعیت PROCESSING)،
 * OrderService این سرویس را با نگاشتِ (شناسه‌ی محصول → تعداد) سفارش صدا می‌زند و برای
 * درمانگرهایی که به آن محصولات لینک شده‌اند (Therapist.productId/messagingProductId)،
 * به‌ترتیب اعتبارِ جلسه یا اشتراکِ پیام‌رسانیِ نامحدود به کاربر اضافه می‌کند.
 */
@Service
class ClinicAccessService(
    private val therapistRepository: TherapistRepository,
    private val creditRepository: SessionCreditRepository,
    private val messagingPlanRepository: MessagingPlanRepository
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

    @Transactional
    fun grantMessagingPlans(userId: Long, productQuantities: Map<Long, Int>) {
        if (productQuantities.isEmpty()) return
        val therapists = therapistRepository.findAll().filter { it.messagingProductId != null && productQuantities.containsKey(it.messagingProductId) }
        for (therapist in therapists) {
            val plan = messagingPlanRepository.findByUserIdAndTherapistId(userId, therapist.id)
                ?: MessagingPlanEntity(userId = userId, therapistId = therapist.id, active = true)
            plan.active = true
            messagingPlanRepository.save(plan)
        }
    }
}
