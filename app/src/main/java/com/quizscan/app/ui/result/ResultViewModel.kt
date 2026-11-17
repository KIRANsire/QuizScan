package com.quizscan.app.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quizscan.app.data.local.ScoreEntity
import com.quizscan.app.data.repository.ScoreRepository
import kotlinx.coroutines.launch

class ResultViewModel(
    private val scoreRepository: ScoreRepository,
    private val savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    val score: Int = savedStateHandle["score"] ?: 0
    val category: String = savedStateHandle["category"] ?: ""
    val difficulty: String = savedStateHandle["difficulty"] ?: ""
    val categoryId: Int = savedStateHandle["categoryId"] ?: 0

    init {
        saveScoreOnce()
    }

    private fun saveScoreOnce() {
        val hasSaved: Boolean = savedStateHandle["hasSaved"] ?: false
        if (hasSaved) return

        val currentUser = auth.currentUser ?: return
        savedStateHandle["hasSaved"] = true

        val entity = ScoreEntity(
            userId = currentUser.uid,
            dateMillis = System.currentTimeMillis(),
            category = category,
            difficulty = difficulty,
            score = score
        )

        viewModelScope.launch {
            scoreRepository.saveScore(entity)
        }
    }
}
