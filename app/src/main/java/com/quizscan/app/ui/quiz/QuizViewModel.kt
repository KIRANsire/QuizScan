package com.quizscan.app.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizscan.app.data.repository.QuizFetchResult
import com.quizscan.app.data.repository.QuizRepository
import com.quizscan.app.model.QuizQuestion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuizLoadState {
    object Loading : QuizLoadState()
    object Success : QuizLoadState()
    object NotEnoughQuestions : QuizLoadState()
    object RateLimited : QuizLoadState()
    data class Error(val message: String) : QuizLoadState()
}

sealed class QuizNavigationEvent {
    data class FinishQuiz(
        val score: Int,
        val category: String,
        val difficulty: String,
        val categoryId: Int
    ) : QuizNavigationEvent()
}

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Int = checkNotNull(savedStateHandle["categoryId"])
    private val difficulty: String = checkNotNull(savedStateHandle["difficulty"])

    private val _loadState = MutableStateFlow<QuizLoadState>(QuizLoadState.Loading)
    val loadState: StateFlow<QuizLoadState> = _loadState.asStateFlow()

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    
    // Persistent state across process death & rotation
    private val _currentQuestionIndex = savedStateHandle.getStateFlow("currentQuestionIndex", 0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _score = savedStateHandle.getStateFlow("score", 0)
    val score: StateFlow<Int> = _score

    private val _selectedOption = savedStateHandle.getStateFlow<String?>("selectedOption", null)
    val selectedOption: StateFlow<String?> = _selectedOption

    private val _isOptionLocked = savedStateHandle.getStateFlow("isOptionLocked", false)
    val isOptionLocked: StateFlow<Boolean> = _isOptionLocked

    private val _navigationEvent = MutableSharedFlow<QuizNavigationEvent>()
    val navigationEvent: SharedFlow<QuizNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _loadState.value = QuizLoadState.Loading
            when (val result = quizRepository.getQuestions(categoryId, difficulty)) {
                is QuizFetchResult.Success -> {
                    _questions.value = result.questions
                    _loadState.value = QuizLoadState.Success
                }
                is QuizFetchResult.NotEnoughQuestions -> {
                    _loadState.value = QuizLoadState.NotEnoughQuestions
                }
                is QuizFetchResult.RateLimited -> {
                    _loadState.value = QuizLoadState.RateLimited
                }
                is QuizFetchResult.Error -> {
                    _loadState.value = QuizLoadState.Error(result.message)
                }
            }
        }
    }

    fun getCurrentQuestion(): QuizQuestion? {
        val list = _questions.value
        val index = _currentQuestionIndex.value
        return if (index in list.indices) list[index] else null
    }

    fun selectOption(option: String) {
        if (_isOptionLocked.value) return

        savedStateHandle["selectedOption"] = option
        savedStateHandle["isOptionLocked"] = true

        val currentQuestion = getCurrentQuestion() ?: return
        if (option == currentQuestion.correctAnswer) {
            val currentScore = _score.value
            savedStateHandle["score"] = currentScore + 1
        }
    }

    fun onNextOrFinishClicked() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _questions.value.size) {
            savedStateHandle["currentQuestionIndex"] = nextIndex
            savedStateHandle["selectedOption"] = null
            savedStateHandle["isOptionLocked"] = false
        } else {
            val categoryName = _questions.value.firstOrNull()?.category ?: ""
            viewModelScope.launch {
                _navigationEvent.emit(
                    QuizNavigationEvent.FinishQuiz(
                        score = _score.value,
                        category = categoryName,
                        difficulty = difficulty,
                        categoryId = categoryId
                    )
                )
            }
        }
    }
}
