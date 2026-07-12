package com.kazemieh.shop.academy.api.dto

data class PlacementQuizOption(val label: String, val score: Int)
data class PlacementQuizQuestion(val id: Int, val text: String, val options: List<PlacementQuizOption>)
data class PlacementQuizResponse(val questions: List<PlacementQuizQuestion>)

data class SubmitPlacementQuizRequest(val answers: List<Int>)
data class PlacementQuizResultResponse(val level: String, val label: String)
