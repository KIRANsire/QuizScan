package com.quizscan.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.quizscan.app.data.repository.CategoryResult
import com.quizscan.app.data.repository.QuizRepository
import com.quizscan.app.model.Category
import com.quizscan.app.util.QrParseResult
import com.quizscan.app.util.QrParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CategoriesUiState {
    object Loading : CategoriesUiState()
    data class Success(val categories: List<Category>) : CategoriesUiState()
    data class Error(val message: String) : CategoriesUiState()
}

sealed class HomeNavigationEvent {
    data class StartQuiz(val categoryId: Int, val difficulty: String) : HomeNavigationEvent()
    data class ShowToast(val message: String) : HomeNavigationEvent()
}

class HomeViewModel(
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val categoriesState: StateFlow<CategoriesUiState> = _categoriesState.asStateFlow()

    private val _navEvents = MutableSharedFlow<HomeNavigationEvent>()
    val navEvents: SharedFlow<HomeNavigationEvent> = _navEvents.asSharedFlow()

    private var cachedCategories: List<Category> = emptyList()

    init {
        loadCategories()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoriesUiState.Loading
            when (val result = quizRepository.getCategories()) {
                is CategoryResult.Success -> {
                    cachedCategories = result.categories
                    _categoriesState.value = CategoriesUiState.Success(result.categories)
                }
                is CategoryResult.Error -> {
                    _categoriesState.value = CategoriesUiState.Error(result.message)
                }
            }
        }
    }

    fun handleQrScanned(rawContent: String?) {
        val parseResult = QrParser.parse(rawContent, cachedCategories)
        viewModelScope.launch {
            when (parseResult) {
                is QrParseResult.Success -> {
                    _navEvents.emit(
                        HomeNavigationEvent.StartQuiz(
                            categoryId = parseResult.categoryId,
                            difficulty = parseResult.difficulty
                        )
                    )
                }
                is QrParseResult.InvalidFormat -> {
                    _navEvents.emit(HomeNavigationEvent.ShowToast(parseResult.reason))
                }
            }
        }
    }

    fun startManualQuiz(categoryId: Int, difficulty: String) {
        viewModelScope.launch {
            _navEvents.emit(HomeNavigationEvent.StartQuiz(categoryId, difficulty))
        }
    }
}
