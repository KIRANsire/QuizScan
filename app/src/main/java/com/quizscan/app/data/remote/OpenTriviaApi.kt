package com.quizscan.app.data.remote

import com.quizscan.app.model.CategoryResponse
import com.quizscan.app.model.QuizResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenTriviaApi {
    @GET("api_category.php")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int = 10,
        @Query("category") categoryId: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String = "multiple"
    ): Response<QuizResponse>
}
