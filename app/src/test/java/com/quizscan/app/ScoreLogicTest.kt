package com.quizscan.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreLogicTest {

    @Test
    fun scoreIncreasesOnlyOnCorrectAnswer() {
        var score = 0
        val correctAnswer = "Paris"

        // Incorrect answers
        val wrongAnswers = listOf("London", "Berlin", "Madrid")
        for (wrong in wrongAnswers) {
            if (wrong == correctAnswer) {
                score++
            }
        }
        assertEquals(0, score)

        // Correct answer
        val chosenAnswer = "Paris"
        if (chosenAnswer == correctAnswer) {
            score++
        }
        assertEquals(1, score)
    }

    @Test
    fun maxScoreIsTenForTenQuestions() {
        var score = 0
        for (i in 1..10) {
            score++
        }
        assertEquals(10, score)
    }
}
