package com.kazemieh.shop.order.recurring

import com.kazemieh.shop.identity.persistence.UserRepository
import com.kazemieh.shop.order.api.dto.CreateOrderItemRequest
import com.kazemieh.shop.order.api.dto.CreateOrderRequest
import com.kazemieh.shop.order.application.OrderService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

data class RecurringOrderView(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val intervalDays: Int,
    val nextRunAt: OffsetDateTime,
    val isActive: Boolean,
    val lastOrderId: Long?
)

@Service
class RecurringOrderService(
    private val recurringOrderRepository: RecurringOrderRepository,
    private val userRepository: UserRepository,
    private val orderService: OrderService
) {
    private val log = LoggerFactory.getLogger(RecurringOrderService::class.java)

    @Transactional
    fun create(userId: Long, variantId: Long, qty: Int, addressId: Long?, intervalDays: Int): RecurringOrderView {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        val entity = recurringOrderRepository.save(
            RecurringOrderEntity(
                user = user,
                variantId = variantId,
                qty = qty,
                addressId = addressId,
                intervalDays = intervalDays,
                nextRunAt = OffsetDateTime.now().plusDays(intervalDays.toLong())
            )
        )
        return entity.toView()
    }

    @Transactional(readOnly = true)
    fun listMine(userId: Long): List<RecurringOrderView> =
        recurringOrderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toView() }

    @Transactional
    fun cancel(userId: Long, id: Long) {
        val entity = recurringOrderRepository.findById(id).orElseThrow { RuntimeException("Recurring order not found") }
        if (entity.user?.id != userId) throw RuntimeException("Access denied")
        entity.isActive = false
        recurringOrderRepository.save(entity)
    }

    /** هر روز اجرا می‌شود؛ برایِ هر اشتراکِ سررسیدشده تلاش می‌کند یک سفارشِ خودکار با کیفِ‌پول بسازد. */
    @Scheduled(fixedRate = 21600000) // هر ۶ ساعت
    @Transactional
    fun runDueRecurringOrders() {
        val due = recurringOrderRepository.findAllByIsActiveTrueAndNextRunAtBefore(OffsetDateTime.now())
        if (due.isEmpty()) return
        log.info("Running {} due recurring orders", due.size)

        for (entity in due) {
            val userId = entity.user?.id ?: continue
            try {
                val response = orderService.create(
                    userId,
                    CreateOrderRequest(
                        addressId = entity.addressId,
                        items = listOf(CreateOrderItemRequest(entity.variantId, entity.qty)),
                        useWallet = true
                    )
                )
                entity.lastOrderId = response.id
            } catch (e: Exception) {
                log.warn("Recurring order {} failed this cycle: {}", entity.id, e.message)
            }
            entity.lastRunAt = OffsetDateTime.now()
            entity.nextRunAt = OffsetDateTime.now().plusDays(entity.intervalDays.toLong())
            recurringOrderRepository.save(entity)
        }
    }

    private fun RecurringOrderEntity.toView() = RecurringOrderView(
        id = id, variantId = variantId, qty = qty, intervalDays = intervalDays,
        nextRunAt = nextRunAt, isActive = isActive, lastOrderId = lastOrderId
    )
}
