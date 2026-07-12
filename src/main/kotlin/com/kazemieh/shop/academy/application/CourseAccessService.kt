package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.entity.EnrollmentEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * اعطای دسترسی به دوره پس از خریدِ محصولِ متناظر.
 *
 * پلِ بینِ ماژولِ سفارش و آموزشگاه: وقتی سفارشی پرداخت می‌شود (وضعیت PROCESSING)،
 * OrderService این سرویس را با شناسه‌ی محصولاتِ سفارش صدا می‌زند و کاربر را به‌طورِ
 * خودکار در دوره‌هایی که به آن محصولات لینک شده‌اند (Course.productId) ثبت‌نام می‌کند.
 * idempotent: اگر کاربر از قبل ثبت‌نام شده باشد، دوباره ثبت نمی‌شود.
 */
@Service
class CourseAccessService(
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository
) {

    @Transactional
    fun grantAccessForProducts(userId: Long, productIds: Collection<Long>) {
        if (productIds.isEmpty()) return
        val courses = courseRepository.findAllByProductIdIn(productIds.toSet())
        for (course in courses) {
            if (!enrollmentRepository.existsByUserIdAndCourseId(userId, course.id)) {
                enrollmentRepository.save(EnrollmentEntity(userId = userId, course = course))
            }
        }
    }
}
