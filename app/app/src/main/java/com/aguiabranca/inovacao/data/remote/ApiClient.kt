package com.aguiabranca.inovacao.data.remote

import com.aguiabranca.inovacao.BuildConfig
import com.aguiabranca.inovacao.data.session.Sessao
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Cliente HTTP da API: injeta o token JWT da sessão em todas as chamadas. */
object ApiClient {

    private val gson = Gson()

    private val autenticacao = okhttp3.Interceptor { chain ->
        val token = Sessao.token
        val requisicao = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(requisicao)
    }

    private val cliente = OkHttpClient.Builder()
        .addInterceptor(autenticacao)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiService::class.java)

    /** Extrai a mensagem do corpo de erro padronizado da API. */
    fun mensagemDeErro(corpo: String?): String? = runCatching {
        corpo?.let { gson.fromJson(it, ErroDto::class.java)?.message }
    }.getOrNull()
}
