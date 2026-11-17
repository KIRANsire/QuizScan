package com.quizscan.app.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryResponse(
    @param:Json(name = "trivia_categories") val triviaCategories: List<Category>
)

@JsonClass(generateAdapter = true)
data class Category(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "name") val name: String
)
