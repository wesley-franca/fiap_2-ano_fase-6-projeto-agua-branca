package com.aguiabranca.inovacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguiabranca.inovacao.data.model.User
import com.aguiabranca.inovacao.data.repository.MockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Preencha email e senha"
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            val result = MockRepository.login(currentState.email, currentState.password)
            result.onSuccess { user ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    currentUser = user,
                    email = currentState.email,
                    isLoggedIn = true
                )
            }.onFailure { exception ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Erro ao fazer login"
                )
            }
        }
    }

    fun logout() {
        MockRepository.logout()
        _uiState.value = AuthUiState()
    }
}
