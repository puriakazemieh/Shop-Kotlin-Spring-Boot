package com.kazemieh.shop.admin

import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.clinic.persistence.TherapistRepository
import com.kazemieh.shop.identity.domain.UserRole
import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.order.persistence.OrderRepository
import com.kazemieh.shop.order.persistence.entity.OrderStatus
import com.kazemieh.shop.psychtest.persistence.PsychTestRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

data class DailySalesResponse(
    val date: String,
    val total: BigDecimal
)

/** تعدادِ آیتم‌های هر عمودی (برای نمایشِ ردِپای هر بخش در داشبورد). */
data class VerticalCountsResponse(
    val courses: Long,
    val therapists: Long,
    val psychTests: Long
)

data class AdminStatsResponse(
    val totalRevenue: BigDecimal,
    val totalOrders: Long,
    val totalProducts: Long,
    val totalCustomers: Long,
    val weeklySales: List<DailySalesResponse>,
    val verticalCounts: VerticalCountsResponse
)

/** آمار داشبورد پنل مدیریت — درآمد، تعداد سفارش/محصول/مشتری، نمودار فروش ۷ روز اخیر و تعدادِ آیتمِ هر عمودی. */
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
class AdminStatsController(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val therapistRepository: TherapistRepository,
    private val psychTestRepository: PsychTestRepository
) {
    @GetMapping
    fun stats(): AdminStatsResponse {
        val cancelled = OrderStatus.CANCELLED
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val from = now.minusDays(6).truncatedTo(ChronoUnit.DAYS)

        val recent = orderRepository.findCreatedSince(from, cancelled)
        val byDay = recent.groupBy { it.createdAt?.toLocalDate()?.toString() ?: "" }
        val weekly = (0..6).map { offset ->
            val date = from.plusDays(offset.toLong()).toLocalDate().toString()
            val total = byDay[date]?.fold(BigDecimal.ZERO) { acc, o -> acc + o.totalPrice } ?: BigDecimal.ZERO
            DailySalesResponse(date = date, total = total)
        }

        return AdminStatsResponse(
            totalRevenue = orderRepository.sumRevenue(cancelled),
            totalOrders = orderRepository.count(),
            totalProducts = productRepository.count(),
            totalCustomers = userRepository.countByRole(UserRole.CUSTOMER),
            weeklySales = weekly,
            verticalCounts = VerticalCountsResponse(
                courses = courseRepository.count(),
                therapists = therapistRepository.count(),
                psychTests = psychTestRepository.count()
            )
        )
    }
}
