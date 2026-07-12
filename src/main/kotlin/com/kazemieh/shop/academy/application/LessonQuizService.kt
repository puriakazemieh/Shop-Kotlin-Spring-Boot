package com.kazemieh.shop.academy.application

import com.kazemieh.shop.academy.api.dto.LessonQuizResponse
import com.kazemieh.shop.academy.api.dto.LessonQuizResultResponse
import com.kazemieh.shop.academy.api.dto.QuizOptionResponse
import com.kazemieh.shop.academy.api.dto.QuizQuestionResponse
import com.kazemieh.shop.academy.api.dto.SubmitLessonQuizRequest
import com.kazemieh.shop.academy.persistence.EnrollmentRepository
import com.kazemieh.shop.academy.persistence.LessonQuizAttemptRepository
import com.kazemieh.shop.academy.persistence.LessonQuizRepository
import com.kazemieh.shop.academy.persistence.LessonRepository
import com.kazemieh.shop.academy.persistence.entity.LessonQuizAttemptEntity
import com.kazemieh.shop.shared.error.ErrorCodes
import com.kazemieh.shop.shared.error.ForbiddenException
import com.kazemieh.shop.shared.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToInt

/**
 * آزمونِ کوتاهِ یک درس (checkpoint) — جداگانه از آزمونِ پایانِ دوره. قبولی در این آزمون
 * گواهی صادر نمی‌کند؛ فقط برای سنجشِ فهمِ همان درس است.
 */
@Service
class LessonQuizService(
    private val lessonQuizRepository: LessonQuizRepository,
    private val lessonQuizAttemptRepository: LessonQuizAttemptRepository,
    private val lessonRepository: LessonRepository,
    private val enrollmentRepository: EnrollmentRepository
) {

    @Transactional(readOnly = true)
    fun getQuiz(lessonId: Long, userId: Long?): LessonQuizResponse {
        val quiz = lessonQuizRepository.findByLessonId(lessonId)
            ?: throw NotFoundException("Lesson quiz not found", ErrorCodes.LESSON_QUIZ_NOT_FOUND)
        val alreadyPassed = userId != null && lessonQuizAttemptRepository.existsByUserIdAndLessonIdAndPassedTrue(userId, lessonId)
        return LessonQuizResponse(
            lessonId = lessonId,
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
    fun submit(userId: Long, lessonId: Long, req: SubmitLessonQuizRequest): LessonQuizResultResponse {
        val quiz = lessonQuizRepository.findByLessonId(lessonId)
            ?: throw NotFoundException("Lesson quiz not found", ErrorCodes.LESSON_QUIZ_NOT_FOUND)
        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { NotFoundException("Lesson not found", ErrorCodes.LESSON_NOT_FOUND) }
        val courseId = lesson.section.course.id
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
        lessonQuizAttemptRepository.save(LessonQuizAttemptEntity(userId = userId, lessonId = lessonId, score = score, passed = passed))
        return LessonQuizResultResponse(lessonId, score, passed, quiz.passScore)
    }
}
