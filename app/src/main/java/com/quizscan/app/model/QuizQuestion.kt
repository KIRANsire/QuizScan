package com.quizscan.app.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuizResponse(
    @param:Json(name = "response_code") val responseCode: Int,
    @param:Json(name = "results") val results: List<QuizQuestionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuizQuestionDto(
    @param:Json(name = "type") val type: String,
    @param:Json(name = "difficulty") val difficulty: String,
    @param:Json(name = "category") val category: String,
    @param:Json(name = "question") val question: String,
    @param:Json(name = "correct_answer") val correctAnswer: String,
    @param:Json(name = "incorrect_answers") val incorrectAnswers: List<String>
)

data class QuizQuestion(
    val category: String,
    val difficulty: String,
    val questionText: String,
    val correctAnswer: String,
    val shuffledOptions: List<String>
)
