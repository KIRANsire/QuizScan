package com.quizscan.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quizscan.app.data.local.ScoreEntity
import com.quizscan.app.data.repository.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val scores: List<ScoreEntity>) : HistoryUiState()
}

class HistoryViewModel(
    private val scoreRepository: ScoreRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = HistoryUiState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            scoreRepository.getScoresForUser(user.uid).collect { list ->
                _uiState.value = HistoryUiState.Success(list)
            }
        }
    }
}
