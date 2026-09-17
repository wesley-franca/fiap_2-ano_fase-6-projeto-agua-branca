package com.aguiabranca.inovacao

import android.app.Application
import com.aguiabranca.inovacao.data.session.Sessao

class InovacaoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Sessao.inicializar(this)
    }
}
