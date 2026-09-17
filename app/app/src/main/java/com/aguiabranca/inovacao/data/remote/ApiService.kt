package com.aguiabranca.inovacao.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("api/auth/me")
    suspend fun me(): UsuarioDto

    @GET("api/orientacoes")
    suspend fun orientacoes(@Query("vigente") vigente: Boolean? = null): List<OrientacaoDto>

    @GET("api/orientacoes/{id}/historico")
    suspend fun historicoOrientacao(@Path("id") id: String): List<HistoricoDto>

    @POST("api/orientacoes")
    suspend fun criarOrientacao(@Body request: OrientacaoRequestDto): OrientacaoDto

    @PUT("api/orientacoes/{id}")
    suspend fun atualizarOrientacao(@Path("id") id: String, @Body request: OrientacaoRequestDto): OrientacaoDto

    @DELETE("api/orientacoes/{id}")
    suspend fun excluirOrientacao(@Path("id") id: String)

    @GET("api/ideias")
    suspend fun ideias(@Query("status") status: String? = null): List<IdeiaDto>

    @GET("api/ideias/{id}")
    suspend fun ideia(@Path("id") id: String): IdeiaDto

    @POST("api/ideias")
    suspend fun criarIdeia(@Body request: NovaIdeiaDto): IdeiaDto

    @PUT("api/ideias/{id}")
    suspend fun atualizarIdeia(@Path("id") id: String, @Body request: NovaIdeiaDto): IdeiaDto

    @DELETE("api/ideias/{id}")
    suspend fun excluirIdeia(@Path("id") id: String)

    @PATCH("api/ideias/{id}/status")
    suspend fun alterarStatusIdeia(@Path("id") id: String, @Body request: StatusIdeiaDto): IdeiaDto

    @PATCH("api/ideias/{id}/prioridade")
    suspend fun priorizarIdeia(@Path("id") id: String, @Body request: PrioridadeRequestDto): IdeiaDto

    @GET("api/projetos")
    suspend fun projetos(): List<ProjetoDto>

    @POST("api/projetos")
    suspend fun criarProjeto(@Body request: ProjetoRequestDto): ProjetoDto

    @PUT("api/projetos/{id}")
    suspend fun atualizarProjeto(@Path("id") id: String, @Body request: ProjetoRequestDto): ProjetoDto

    @PATCH("api/projetos/{id}/progresso")
    suspend fun registrarProgresso(@Path("id") id: String, @Body request: ProgressoRequestDto): ProjetoDto

    @PATCH("api/projetos/{id}/resultados")
    suspend fun registrarResultados(@Path("id") id: String, @Body request: ResultadosRequestDto): ProjetoDto

    @DELETE("api/projetos/{id}")
    suspend fun excluirProjeto(@Path("id") id: String)

    @GET("api/dashboard/gestor")
    suspend fun painelGestor(): PainelGestorDto

    @GET("api/dashboard/resumo")
    suspend fun resumoLideranca(): ResumoLiderancaDto

    @GET("api/dashboard/projetos/{id}")
    suspend fun resumoProjeto(@Path("id") id: String): ResumoProjetoDto
}
