package com.kazemieh.shop.psychtest.application

import com.kazemieh.shop.psychtest.api.dto.*
import com.kazemieh.shop.psychtest.persistence.PsychTestRepository
import com.kazemieh.shop.psychtest.persistence.UserPsychTestRepository
import com.kazemieh.shop.psychtest.persistence.entity.*
import com.kazemieh.shop.shared.error.ConflictException
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class AdminPsychTestService(
    private val testRepository: PsychTestRepository,
    private val userTestRepository: UserPsychTestRepository
) {

    @Transactional(readOnly = true)
    fun list(): List<PsychTestSummaryResponse> = testRepository.findAll().map {
        PsychTestSummaryResponse(
            id = it.id, title = it.title, slug = it.slug, description = it.description, price = it.price,
            discountedPrice = it.discountedPrice, resultMode = it.resultMode.name,
            questionCount = it.questions.size, owned = false, productId = it.productId
        )
    }

    @Transactional
    fun create(req: AdminCreatePsychTestRequest): Long {
        val slug = req.slug.trim()
        if (testRepository.existsBySlug(slug)) throw ConflictException("Slug exists", ErrorCodes.PSYCH_TEST_SLUG_EXISTS)
        val test = PsychTestEntity(
            title = req.title.trim(),
            slug = slug,
            description = req.description,
            price = req.price,
            discountedPrice = req.discountedPrice,
            productId = req.productId,
            resultMode = parseMode(req.resultMode),
            isPublished = req.isPublished,
            questions = req.questions.map { it.toEntity() }.toMutableList(),
            ranges = req.ranges.map { ScoreRange(it.minScore, it.maxScore, it.interpretation) }.toMutableList()
        )
        return testRepository.save(test).id
    }

    /** جزئیاتِ کاملِ یک تست (با امتیازِ گزینه‌ها و بازه‌ها) برای پیش‌پُر کردنِ فرمِ ویرایش. */
    @Transactional(readOnly = true)
    fun detail(id: Long): AdminPsychTestDetailResponse {
        val test = findTest(id)
        return AdminPsychTestDetailResponse(
            id = test.id, title = test.title, slug = test.slug, description = test.description,
            price = test.price, discountedPrice = test.discountedPrice, productId = test.productId,
            resultMode = test.resultMode.name, isPublished = test.isPublished,
            questions = test.questions.mapIndexed { i, q ->
                TestQuestionResponse(i, q.text, q.options.map { TestOptionResponse(it.text, score = it.score) })
            },
            ranges = test.ranges.map { ScoreRangeResponse(it.minScore, it.maxScore, it.interpretation) }
        )
    }

    @Transactional
    fun update(id: Long, req: AdminUpdatePsychTestRequest) {
        val test = findTest(id)
        req.title?.let { test.title = it.trim() }
        req.description?.let { test.description = it }
        req.price?.let { test.price = it }
        req.discountedPrice?.let { test.discountedPrice = it }
        req.productId?.let { test.productId = it }
        req.resultMode?.let { test.resultMode = parseMode(it) }
        req.isPublished?.let { test.isPublished = it }
        req.questions?.let { qs -> test.questions = qs.map { it.toEntity() }.toMutableList() }
        req.ranges?.let { rs -> test.ranges = rs.map { ScoreRange(it.minScore, it.maxScore, it.interpretation) }.toMutableList() }
        testRepository.save(test)
    }

    @Transactional
    fun delete(id: Long) = testRepository.delete(findTest(id))

    /** فهرستِ تست‌های در انتظارِ تفسیرِ مشاور. */
    @Transactional(readOnly = true)
    fun pendingInterpretations(): List<UserPsychTestResponse> {
        val pending = userTestRepository.findAllByStatus(UserTestStatus.AWAITING_INTERPRETATION)
        val testsById = testRepository.findAllById(pending.map { it.testId }.distinct()).associateBy { it.id }
        return pending.map { ut ->
            val test = testsById[ut.testId]
            UserPsychTestResponse(
                id = ut.id, testId = ut.testId, testTitle = test?.title ?: "تست",
                status = ut.status.name, resultMode = test?.resultMode?.name ?: "COUNSELOR",
                totalScore = ut.totalScore, interpretation = ut.interpretation,
                completedAt = ut.completedAt?.toString()
            )
        }
    }

    @Transactional
    fun interpret(counselorId: Long, userTestId: Long, req: AdminInterpretRequest) {
        val ut = userTestRepository.findById(userTestId)
            .orElseThrow { NotFoundException("User test not found", ErrorCodes.USER_TEST_NOT_FOUND) }
        ut.interpretation = req.interpretation
        ut.interpretedByCounselorId = counselorId
        ut.status = UserTestStatus.COMPLETED
        ut.completedAt = OffsetDateTime.now()
        userTestRepository.save(ut)
    }

    private fun findTest(id: Long): PsychTestEntity =
        testRepository.findById(id).orElseThrow { NotFoundException("Test not found", ErrorCodes.PSYCH_TEST_NOT_FOUND) }

    private fun parseMode(v: String?): TestResultMode =
        runCatching { TestResultMode.valueOf(v!!.trim().uppercase()) }.getOrDefault(TestResultMode.AUTO)

    private fun TestQuestionResponse.toEntity() = TestQuestion(
        text = text,
        options = options.map { TestOption(it.text, it.score ?: 0) }.toMutableList()
    )
}
