package com.kazemieh.shop.academy.api

import com.kazemieh.shop.academy.api.dto.*
import org.springframework.web.bind.annotation.*

/**
 * آزمونِ تعیینِ سطحِ ثابت (بدونِ نیازِ مدلِ داده‌ی جدید) — بر اساسِ مجموعِ امتیازِ گزینه‌ها،
 * سطحِ پیشنهادی (BEGINNER/INTERMEDIATE/ADVANCED) را برمی‌گرداند تا کاربر بتواند دوره‌ها را
 * بر همان اساس در کلاینت فیلتر کند (فیلدِ `level` از قبل روی هر دوره هست).
 */
@RestController
@RequestMapping("/api/academy/placement-quiz")
class PlacementQuizController {

    private val questions = listOf(
        PlacementQuizQuestion(
            1, "چقدر با این حوزه آشنایی داری؟",
            listOf(PlacementQuizOption("اصلاً", 1), PlacementQuizOption("کمی", 2), PlacementQuizOption("خیلی", 3))
        ),
        PlacementQuizQuestion(
            2, "قبلاً دوره یا پروژه‌ی مرتبطی انجام داده‌ای؟",
            listOf(PlacementQuizOption("نه", 1), PlacementQuizOption("یکی-دوتا", 2), PlacementQuizOption("چندین‌بار", 3))
        ),
        PlacementQuizQuestion(
            3, "هدفت از این دوره چیست؟",
            listOf(PlacementQuizOption("شروعِ از صفر", 1), PlacementQuizOption("تقویتِ پایه", 2), PlacementQuizOption("تسلطِ حرفه‌ای", 3))
        )
    )

    @GetMapping
    fun get(): PlacementQuizResponse = PlacementQuizResponse(questions)

    @PostMapping("/submit")
    fun submit(@RequestBody request: SubmitPlacementQuizRequest): PlacementQuizResultResponse {
        val total = request.answers.mapIndexed { index, optionIndex ->
            questions.getOrNull(index)?.options?.getOrNull(optionIndex)?.score ?: 0
        }.sum()
        return when {
            total <= 4 -> PlacementQuizResultResponse("BEGINNER", "مبتدی")
            total <= 7 -> PlacementQuizResultResponse("INTERMEDIATE", "متوسط")
            else -> PlacementQuizResultResponse("ADVANCED", "پیشرفته")
        }
    }
}
