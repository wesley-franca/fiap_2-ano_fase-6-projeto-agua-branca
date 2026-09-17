package com.aguiabranca.inovacao.data.session

import android.content.Context
import com.aguiabranca.inovacao.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Sessão do usuário: mantida em memória para as chamadas HTTP e no disco para sobreviver ao fechamento do app. */
object Sessao {

    private var store: SessaoStore? = null

    var token: String? = null
        private set

    var usuario: User? = null
        private set

    private val _expirada = MutableStateFlow(false)

    /** Vira `true` quando a API recusa o token; a navegação usa isso para voltar ao login. */
    val expirada: StateFlow<Boolean> = _expirada

    fun inicializar(context: Context) {
        store = SessaoStore(context.applicationContext)
    }

    suspend fun iniciar(token: String, usuario: User) {
        this.token = token
        this.usuario = usuario
        _expirada.value = false
        store?.salvar(token, usuario)
    }

    /** Recarrega a sessão salva em disco. Retorna o usuário salvo, ainda sem validar o token na API. */
    suspend fun restaurar(): User? {
        val salvo = store?.ler() ?: return null
        token = salvo.first
        usuario = salvo.second
        return salvo.second
    }

    fun atualizarUsuario(usuario: User) {
        this.usuario = usuario
    }

    suspend fun encerrar() {
        token = null
        usuario = null
        store?.limpar()
    }

    /** Chamado quando a API responde 401: derruba a sessão e avisa a navegação. */
    suspend fun expirar() {
        encerrar()
        _expirada.value = true
    }

    fun expiracaoTratada() {
        _expirada.value = false
    }
}
