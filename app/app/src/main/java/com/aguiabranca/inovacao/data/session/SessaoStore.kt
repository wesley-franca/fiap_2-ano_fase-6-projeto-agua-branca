package com.aguiabranca.inovacao.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aguiabranca.inovacao.data.model.User
import com.aguiabranca.inovacao.data.model.UserRole
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sessao")

/** Guarda o token e o usuário no disco para manter o login ao reabrir o app. */
class SessaoStore(private val context: Context) {

    private val chaveToken = stringPreferencesKey("token")
    private val chaveId = stringPreferencesKey("usuario_id")
    private val chaveNome = stringPreferencesKey("usuario_nome")
    private val chaveEmail = stringPreferencesKey("usuario_email")
    private val chaveRole = stringPreferencesKey("usuario_role")
    private val chaveArea = stringPreferencesKey("usuario_area")

    suspend fun salvar(token: String, usuario: User) {
        context.dataStore.edit { prefs ->
            prefs[chaveToken] = token
            prefs[chaveId] = usuario.uid
            prefs[chaveNome] = usuario.nome
            prefs[chaveEmail] = usuario.email
            prefs[chaveRole] = usuario.role.name
            prefs[chaveArea] = usuario.area
        }
    }

    suspend fun ler(): Pair<String, User>? {
        val prefs = context.dataStore.data.first()
        val token = prefs[chaveToken] ?: return null
        val role = runCatching { UserRole.valueOf(prefs[chaveRole].orEmpty()) }.getOrNull() ?: return null

        return token to User(
            uid = prefs[chaveId].orEmpty(),
            email = prefs[chaveEmail].orEmpty(),
            nome = prefs[chaveNome].orEmpty(),
            role = role,
            area = prefs[chaveArea].orEmpty()
        )
    }

    suspend fun limpar() {
        context.dataStore.edit { it.clear() }
    }
}
