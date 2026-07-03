package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.*
import com.kazemieh.shop.academy.persistence.CertificateRepository
import com.kazemieh.shop.academy.persistence.CourseRepository
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.QuizAttemptRepository
import com.kazemieh.shop.academy.persistence.QuizRepository
import com.kazemieh.shop.academy.persistence.entity.CertificateEntity
import com.kazemieh.shop.academy.persistence.entity.QuizAttemptEntity
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToInt

/**
 * آزمونِ پایانِ دوره + صدورِ گواهی. کاربر باید ثبت‌نام کرده باشد. قبولی (score ≥ passScore)
 * به‌صورتِ idempotent یک گواهی صادر می‌کند.
 */
@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val quizAttemptRepository: QuizAttemptRepository,
    private val certificateRepository: CertificateRepository,
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository
) {

    /** آزمونِ عمومی — بدونِ افشایِ پاسخِ درست (correct = null). */
    @Transactional(readOnly = true)
    fun getQuiz(courseId: Long, userId: Long?): QuizResponse {
        val quiz = quizRepository.findByCourseId(courseId)
            ?: throw NotFoundException("Quiz not found", ErrorCodes.QUIZ_NOT_FOUND)
        val alreadyPassed = userId != null && quizAttemptRepository.existsByUserIdAndCourseIdAndPassedTrue(userId, courseId)
        return QuizResponse(
            courseId = courseId,
            title = quiz.title,
            passScore = quiz.passScore,
            questions = quiz.questions.mapIndexed { i, q ->
                QuizQuestionResponse(
                    index = i,
                    text = q.text,
                    options = q.options.map { QuizOptionResponse(text = it.text, correct = null) }
                )
            },
            alreadyPassed = alreadyPassed
        )
    }

    @Transactional
    fun submit(userId: Long, courseId: Long, req: SubmitQuizRequest): QuizResultResponse {
        val quiz = quizRepository.findByCourseId(courseId)
            ?: throw NotFoundException("Quiz not found", ErrorCodes.QUIZ_NOT_FOUND)
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw ForbiddenException("Not enrolled in this course", ErrorCodes.NOT_ENROLLED)
        }
        val total = quiz.questions.size
        val correct = quiz.questions.withIndex().count { (i, q) ->
            val chosen = req.answers[i] ?: -1
            q.options.getOrNull(chosen)?.correct == true
        }
        val score = if (total == 0) 0 else ((correct * 100.0) / total).roundToInt()
        val passed = score >= quiz.passScore
        quizAttemptRepository.save(QuizAttemptEntity(userId = userId, courseId = courseId, score = score, passed = passed))

        var certNumber: String? = null
        if (passed) {
            val existing = certificateRepository.findByUserIdAndCourseId(userId, courseId)
            certNumber = existing?.certNumber ?: run {
                val number = generateCertNumber(courseId, userId)
                certificateRepository.save(CertificateEntity(userId = userId, courseId = courseId, certNumber = number))
                number
            }
        }
        return QuizResultResponse(courseId, score, passed, quiz.passScore, certNumber)
    }

    @Transactional(readOnly = true)
    fun myCertificates(userId: Long): List<CertificateResponse> =
        certificateRepository.findAllByUserIdOrderByIssuedAtDesc(userId).map { cert ->
            val course = courseRepository.findById(cert.courseId).orElse(null)
            CertificateResponse(
                id = cert.id,
                courseId = cert.courseId,
                courseTitle = course?.title ?: "دوره",
                certNumber = cert.certNumber,
                issuedAt = cert.issuedAt.toString()
            )
        }

    private fun generateCertNumber(courseId: Long, userId: Long): String {
        val ts = System.currentTimeMillis().toString(36).uppercase()
        return "CERT-$courseId-$userId-$ts"
    }
}
