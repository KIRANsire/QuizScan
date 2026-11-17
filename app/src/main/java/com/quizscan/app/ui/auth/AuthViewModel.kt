package com.quizscan.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    object Cancelled : AuthUiState()
    object NoNetwork : AuthUiState()
    data class Failure(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun handleSignInCancelled() {
        _uiState.value = AuthUiState.Cancelled
    }

    fun handleSignInFailure(e: Exception) {
        if (e is IOException || e.message?.contains("network", ignoreCase = true) == true) {
            _uiState.value = AuthUiState.NoNetwork
        } else {
            _uiState.value = AuthUiState.Failure(e.message ?: "Authentication failed")
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Failure("User is null")
                }
            } catch (e: Exception) {
                if (e is IOException || e.message?.contains("network", ignoreCase = true) == true) {
                    _uiState.value = AuthUiState.NoNetwork
                } else {
                    _uiState.value = AuthUiState.Failure(e.message ?: "Sign-in failed")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
