package com.quizscan.app.util

import com.quizscan.app.model.Category

sealed class QrParseResult {
    data class Success(val categoryId: Int, val difficulty: String) : QrParseResult()
    data class InvalidFormat(val reason: String) : QrParseResult()
}

object QrParser {
    private val VALID_DIFFICULTIES = setOf("easy", "medium", "hard")

    fun parse(rawText: String?, availableCategories: List<Category>): QrParseResult {
        if (rawText.isNullOrBlank()) {
            return QrParseResult.InvalidFormat("QR code is empty")
        }

        val parts = rawText.split(";")
        var categoryId: Int? = null
        var difficulty: String? = null

        for (part in parts) {
            val trimmed = part.trim()
            val keyValue = trimmed.split("=")
            if (keyValue.size == 2) {
                val key = keyValue[0].trim().lowercase()
                val value = keyValue[1].trim()

                when (key) {
                    "category" -> {
                        categoryId = value.toIntOrNull()
                    }
                    "difficulty" -> {
                        difficulty = value.lowercase()
                    }
                }
            }
        }

        if (categoryId == null) {
            return QrParseResult.InvalidFormat("Missing or invalid category ID")
        }

        if (difficulty == null || !VALID_DIFFICULTIES.contains(difficulty)) {
            return QrParseResult.InvalidFormat("Difficulty must be easy, medium, or hard")
        }

        val categoryExists = availableCategories.any { it.id == categoryId }
        if (!categoryExists) {
            return QrParseResult.InvalidFormat("Category ID $categoryId does not exist in available categories")
        }

        return QrParseResult.Success(categoryId = categoryId, difficulty = difficulty)
    }
}
