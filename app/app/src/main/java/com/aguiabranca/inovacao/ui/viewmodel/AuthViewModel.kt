package com.aguiabranca.inovacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguiabranca.inovacao.data.model.User
import com.aguiabranca.inovacao.data.repository.InovacaoRepository
import com.aguiabranca.inovacao.data.session.Sessao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    /** Verdadeiro enquanto o app confere se existe uma sessão salva. */
    val isRestoring: Boolean = true,
    val email: String = "",
    val password: String = "",
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        restaurarSessao()
    }

    private fun restaurarSessao() {
        viewModelScope.launch {
            val usuario = InovacaoRepository.restaurarSessao()
            _uiState.value = if (usuario != null) {
                AuthUiState(isRestoring = false, currentUser = usuario, email = usuario.email, isLoggedIn = true)
            } else {
                AuthUiState(isRestoring = false)
            }
        }
    }

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
                        isRestoring = false,
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
        viewModelScope.launch {
            InovacaoRepository.logout()
            _uiState.value = AuthUiState(isRestoring = false)
        }
    }

    /** Chamado quando a API recusa o token durante o uso do app. */
    fun sessaoExpirou() {
        Sessao.expiracaoTratada()
        _uiState.value = AuthUiState(
            isRestoring = false,
            errorMessage = "Sua sessão expirou. Entre novamente."
        )
    }
}
