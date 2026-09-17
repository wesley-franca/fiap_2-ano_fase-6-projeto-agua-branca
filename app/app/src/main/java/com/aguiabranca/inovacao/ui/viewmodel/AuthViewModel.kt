package com.aguiabranca.inovacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguiabranca.inovacao.data.model.User
import com.aguiabranca.inovacao.data.repository.InovacaoRepository
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
        val estado = _uiState.value
        if (estado.email.isBlank() || estado.password.isBlank()) {
            _uiState.value = estado.copy(errorMessage = "Preencha e-mail e senha")
            return
        }

        _uiState.value = estado.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            InovacaoRepository.login(estado.email, estado.password)
                .onSuccess { usuario ->
                    _uiState.value = AuthUiState(
                        currentUser = usuario,
                        email = estado.email,
                        isLoggedIn = true
                    )
                }
                .onFailure { erro ->
                    _uiState.value = estado.copy(
                        isLoading = false,
                        errorMessage = erro.message ?: "Não foi possível entrar"
                    )
                }
        }
    }

    fun logout() {
        InovacaoRepository.logout()
        _uiState.value = AuthUiState()
    }
}
