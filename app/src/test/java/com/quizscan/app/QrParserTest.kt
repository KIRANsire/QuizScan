package com.quizscan.app

import com.quizscan.app.model.Category
import com.quizscan.app.util.QrParseResult
import com.quizscan.app.util.QrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrParserTest {

    private val categories = listOf(
        Category(id = 9, name = "General Knowledge"),
        Category(id = 18, name = "Science: Computers"),
        Category(id = 21, name = "Sports")
    )

    @Test
    fun parse_validFormat_returnsSuccess() {
        val input = "category=9;difficulty=easy"
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals(9, success.categoryId)
        assertEquals("easy", success.difficulty)
    }

    @Test
    fun parse_reversedKeyOrder_returnsSuccess() {
        val input = "difficulty=hard;category=18"
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals(18, success.categoryId)
        assertEquals("hard", success.difficulty)
    }

    @Test
    fun parse_withWhitespaceAndCaseVariation_returnsSuccess() {
        val input = "  category = 21 ; difficulty = Medium  "
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals(21, success.categoryId)
        assertEquals("medium", success.difficulty)
    }

    @Test
    fun parse_categoryNotInList_returnsInvalid() {
        val input = "category=999;difficulty=easy"
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.InvalidFormat)
    }

    @Test
    fun parse_invalidDifficulty_returnsInvalid() {
        val input = "category=9;difficulty=insane"
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.InvalidFormat)
    }

    @Test
    fun parse_missingCategory_returnsInvalid() {
        val input = "difficulty=easy"
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.InvalidFormat)
    }

    @Test
    fun parse_nonNumericCategory_returnsInvalid() {
        val input = "category=nine;difficulty=easy"
        val result = QrParser.parse(input, categories)

        assertTrue(result is QrParseResult.InvalidFormat)
    }

    @Test
    fun parse_emptyInput_returnsInvalid() {
        val result = QrParser.parse("", categories)
        assertTrue(result is QrParseResult.InvalidFormat)
    }
}
