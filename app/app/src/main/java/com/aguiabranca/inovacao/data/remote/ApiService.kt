package com.aguiabranca.inovacao.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("api/auth/me")
    suspend fun me(): UsuarioDto

    @GET("api/orientacoes")
    suspend fun orientacoes(@Query("vigente") vigente: Boolean? = null): List<OrientacaoDto>

    @GET("api/ideias")
    suspend fun ideias(@Query("status") status: String? = null): List<IdeiaDto>

    @POST("api/ideias")
    suspend fun criarIdeia(@Body request: NovaIdeiaDto): IdeiaDto

    @PATCH("api/ideias/{id}/status")
    suspend fun alterarStatusIdeia(@Path("id") id: String, @Body request: StatusIdeiaDto): IdeiaDto

    @GET("api/projetos")
    suspend fun projetos(): List<ProjetoDto>

    @GET("api/dashboard/gestor")
    suspend fun painelGestor(): PainelGestorDto

    @GET("api/dashboard/resumo")
    suspend fun resumoLideranca(): ResumoLiderancaDto
}
