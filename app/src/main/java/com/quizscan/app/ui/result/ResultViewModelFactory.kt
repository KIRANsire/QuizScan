package com.quizscan.app.ui.result

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.quizscan.app.data.repository.ScoreRepository

class ResultViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val scoreRepository: ScoreRepository,
    defaultArgs: android.os.Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(scoreRepository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
