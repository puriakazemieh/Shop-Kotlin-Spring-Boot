package com.kazemieh.shop.psychtest.application

import com.kazemieh.shop.catalog.persistence.ProductRepository
import com.kazemieh.shop.psychtest.api.dto.*
import com.kazemieh.shop.psychtest.persistence.PsychTestRepository
import com.kazemieh.shop.psychtest.persistence.UserPsychTestRepository
import com.kazemieh.shop.psychtest.persistence.entity.PsychTestEntity
import com.kazemieh.shop.psychtest.persistence.entity.TestResultMode
import com.kazemieh.shop.psychtest.persistence.entity.UserPsychTestEntity
import com.kazemieh.shop.psychtest.persistence.entity.UserTestStatus
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/** کاتالوگِ عمومیِ تست‌ها + «تست‌های من» + انجامِ تست. */
@Service
class PsychTestService(
    private val testRepository: PsychTestRepository,
    private val userTestRepository: UserPsychTestRepository,
    private val productRepository: ProductRepository
) {

    @Transactional(readOnly = true)
    fun listTests(userId: Long?): List<PsychTestSummaryResponse> {
        val owned = ownedTestIds(userId)
        return testRepository.findAllByIsPublishedTrueOrderByCreatedAtDesc().map { it.toSummary(it.id in owned) }
    }

    @Transactional(readOnly = true)
    fun getTest(slug: String, userId: Long?): PsychTestDetailResponse {
        val test = testRepository.findBySlug(slug)
            ?: throw NotFoundException("Test not found", ErrorCodes.PSYCH_TEST_NOT_FOUND)
        val owned = test.id in ownedTestIds(userId)
        return PsychTestDetailResponse(
            id = test.id, title = test.title, slug = test.slug, description = test.description,
            price = test.price, discountedPrice = test.discountedPrice, resultMode = test.resultMode.name,
            owned = owned, productId = test.productId,
            questions = test.questions.mapIndexed { i, q ->
                TestQuestionResponse(i, q.text, q.options.map { TestOptionResponse(it.text, score = null) })
            }
        )
    }

    @Transactional(readOnly = true)
    fun myTests(userId: Long): List<UserPsychTestResponse> {
        val userTests = userTestRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        val testsById = testRepository.findAllById(userTests.map { it.testId }.distinct()).associateBy { it.id }
        return userTests.map { ut ->
            val test = testsById[ut.testId]
            ut.toResponse(test)
        }
    }

    /** سؤالاتِ یک تستِ خریداری‌شده برای انجام (بدونِ افشایِ امتیازِ گزینه‌ها). */
    @Transactional(readOnly = true)
    fun getUserTestQuestions(userId: Long, userTestId: Long): PsychTestDetailResponse {
        val ut = userTestRepository.findByIdAndUserId(userTestId, userId)
            ?: throw NotFoundException("User test not found", ErrorCodes.USER_TEST_NOT_FOUND)
        val test = testRepository.findById(ut.testId)
            .orElseThrow { NotFoundException("Test not found", ErrorCodes.PSYCH_TEST_NOT_FOUND) }
        return PsychTestDetailResponse(
            id = test.id, title = test.title, slug = test.slug, description = test.description,
            price = test.price, discountedPrice = test.discountedPrice, resultMode = test.resultMode.name,
            owned = true, productId = test.productId,
            questions = test.questions.mapIndexed { i, q ->
                TestQuestionResponse(i, q.text, q.options.map { TestOptionResponse(it.text, score = null) })
            }
        )
    }

    @Transactional
    fun submit(userId: Long, userTestId: Long, req: SubmitTestRequest): UserPsychTestResponse {
        val ut = userTestRepository.findByIdAndUserId(userTestId, userId)
            ?: throw NotFoundException("User test not found", ErrorCodes.USER_TEST_NOT_FOUND)
        val test = testRepository.findById(ut.testId)
            .orElseThrow { NotFoundException("Test not found", ErrorCodes.PSYCH_TEST_NOT_FOUND) }

        val totalScore = test.questions.withIndex().sumOf { (i, q) ->
            val chosen = req.answers[i] ?: -1
            q.options.getOrNull(chosen)?.score ?: 0
        }
        ut.totalScore = totalScore

        if (test.resultMode == TestResultMode.AUTO) {
            val range = test.ranges.firstOrNull { totalScore in it.minScore..it.maxScore }
            ut.interpretation = range?.interpretation ?: "نتیجه در بازه‌ی تعریف‌شده قرار نگرفت."
            ut.status = UserTestStatus.COMPLETED
            ut.completedAt = OffsetDateTime.now()
        } else {
            // تفسیرِ دستی توسطِ مشاور
            ut.status = UserTestStatus.AWAITING_INTERPRETATION
        }
        userTestRepository.save(ut)
        return ut.toResponse(test)
    }

    private fun ownedTestIds(userId: Long?): Set<Long> {
        if (userId == null) return emptySet()
        return userTestRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.testId }.toSet()
    }

    private fun PsychTestEntity.toSummary(owned: Boolean) = PsychTestSummaryResponse(
        id = id, title = title, slug = slug, description = description, price = price,
        discountedPrice = discountedPrice, resultMode = resultMode.name,
        questionCount = questions.size, owned = owned, productId = productId,
        productSlug = productId?.let { productRepository.findById(it).orElse(null)?.slug }
    )

    private fun UserPsychTestEntity.toResponse(test: PsychTestEntity?) = UserPsychTestResponse(
        id = id, testId = testId, testTitle = test?.title ?: "تست",
        status = status.name, resultMode = test?.resultMode?.name ?: "AUTO",
        totalScore = totalScore, interpretation = interpretation,
        completedAt = completedAt?.toString()
    )
}
