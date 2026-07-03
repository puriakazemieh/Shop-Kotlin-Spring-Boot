package com.kazemieh.shop.psychtest.application

import com.kazemieh.shop.psychtest.persistence.PsychTestRepository
import com.kazemieh.shop.psychtest.persistence.UserPsychTestRepository
import com.kazemieh.shop.psychtest.persistence.entity.UserPsychTestEntity
import com.kazemieh.shop.psychtest.persistence.entity.UserTestStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * اعطای دسترسیِ تست پس از خریدِ محصولِ متناظر. پلِ بینِ سفارش و ماژولِ تست:
 * برای هر واحدِ خریداری‌شده از محصولِ لینک‌شده، یک نمونه‌ی UserPsychTest(status=PURCHASED)
 * ساخته می‌شود تا کاربر بتواند تست را انجام دهد.
 */
@Service
class PsychTestAccessService(
    private val testRepository: PsychTestRepository,
    private val userTestRepository: UserPsychTestRepository
) {

    @Transactional
    fun grantTestAccess(userId: Long, productQuantities: Map<Long, Int>) {
        if (productQuantities.isEmpty()) return
        val tests = testRepository.findAllByProductIdIn(productQuantities.keys)
        for (test in tests) {
            val qty = productQuantities[test.productId] ?: continue
            repeat(qty.coerceAtLeast(0)) {
                userTestRepository.save(
                    UserPsychTestEntity(userId = userId, testId = test.id, status = UserTestStatus.PURCHASED)
                )
            }
        }
    }
}
