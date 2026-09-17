package com.aguiabranca.inovacao.data.session

import com.aguiabranca.inovacao.data.model.User

/**
 * Sessão do usuário durante a execução do app.
 * A persistência em disco (manter o login ao reabrir) entra junto com a tela de login definitiva.
 */
object Sessao {

    var token: String? = null
        private set

    var usuario: User? = null
        private set

    fun iniciar(token: String, usuario: User) {
        this.token = token
        this.usuario = usuario
    }

    fun encerrar() {
        token = null
        usuario = null
    }
}
