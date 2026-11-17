package com.quizscan.app.data.repository

import com.quizscan.app.data.remote.OpenTriviaApi
import com.quizscan.app.data.remote.RetrofitClient
import com.quizscan.app.model.Category
import com.quizscan.app.model.QuizQuestion
import com.quizscan.app.util.HtmlDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

sealed class CategoryResult {
    data class Success(val categories: List<Category>) : CategoryResult()
    data class Error(val message: String) : CategoryResult()
}

sealed class QuizFetchResult {
    data class Success(val questions: List<QuizQuestion>) : QuizFetchResult()
    object NotEnoughQuestions : QuizFetchResult()
    object RateLimited : QuizFetchResult()
    data class Error(val message: String) : QuizFetchResult()
}

class QuizRepository(
    private val api: OpenTriviaApi = RetrofitClient.api
) {
    suspend fun getCategories(): CategoryResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.triviaCategories.sortedBy { it.name }
                CategoryResult.Success(list)
            } else {
                CategoryResult.Error("Server returned code ${response.code()}")
            }
        } catch (e: IOException) {
            CategoryResult.Error(e.message ?: "Network error")
        } catch (e: Exception) {
            CategoryResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getQuestions(categoryId: Int, difficulty: String): QuizFetchResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getQuestions(
                amount = 10,
                categoryId = categoryId,
                difficulty = difficulty
            )

            if (response.code() == 429) {
                return@withContext QuizFetchResult.RateLimited
            }

            if (!response.isSuccessful || response.body() == null) {
                return@withContext QuizFetchResult.Error("Server returned code ${response.code()}")
            }

            val body = response.body()!!
            when (body.responseCode) {
                0 -> {
                    val questions = body.results.map { dto ->
                        val decodedCorrect = HtmlDecoder.decode(dto.correctAnswer)
                        val decodedIncorrect = dto.incorrectAnswers.map { HtmlDecoder.decode(it) }
                        val options = (decodedIncorrect + decodedCorrect).shuffled()

                        QuizQuestion(
                            category = HtmlDecoder.decode(dto.category),
                            difficulty = dto.difficulty,
                            questionText = HtmlDecoder.decode(dto.question),
                            correctAnswer = decodedCorrect,
                            shuffledOptions = options
                        )
                    }
                    QuizFetchResult.Success(questions)
                }
                1, 2 -> QuizFetchResult.NotEnoughQuestions
                5 -> QuizFetchResult.RateLimited
                else -> QuizFetchResult.Error("API returned response code ${body.responseCode}")
            }
        } catch (e: IOException) {
            QuizFetchResult.Error(e.message ?: "Network error")
        } catch (e: Exception) {
            QuizFetchResult.Error(e.message ?: "Unknown error")
        }
    }
}
