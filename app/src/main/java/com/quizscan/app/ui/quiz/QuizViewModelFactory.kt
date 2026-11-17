package com.quizscan.app.ui.quiz

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.quizscan.app.data.repository.QuizRepository

class QuizViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val quizRepository: QuizRepository,
    defaultArgs: android.os.Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            return QuizViewModel(quizRepository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
