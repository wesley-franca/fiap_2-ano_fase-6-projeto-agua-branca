package com.aguiabranca.inovacao.data.remote

import com.aguiabranca.inovacao.data.model.IdeaPriority
import com.aguiabranca.inovacao.data.model.IdeaStatus
import com.aguiabranca.inovacao.data.model.UserRole

// ----- Autenticação -----

data class LoginRequestDto(val email: String, val senha: String)

data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEm: String,
    val usuario: UsuarioDto
)

data class UsuarioDto(
    val id: String,
    val nome: String,
    val email: String,
    val role: UserRole,
    val area: String?
)

// ----- Orientações -----

data class OrientacaoDto(
    val id: String,
    val titulo: String,
    val descricao: String?,
    val categoria: String?,
    val campanha: String?,
    val area: String?,
    val periodo: String?,
    val indicadores: List<String>?,
    val vigente: Boolean
)

// ----- Ideias -----

data class IdeiaDto(
    val id: String,
    val titulo: String,
    val categoria: String?,
    val problemaObservado: String?,
    val suaProposta: String?,
    val impacto: String?,
    val status: IdeaStatus,
    val prioridade: IdeaPriority,
    val operadorId: String?,
    val nomeOperador: String?,
    val area: String?,
    val orientacaoId: String?,
    val comentarioAvaliacao: String?,
    val criadoEm: String?
)

data class NovaIdeiaDto(
    val titulo: String,
    val categoria: String,
    val problemaObservado: String,
    val suaProposta: String,
    val orientacaoId: String
)

data class StatusIdeiaDto(val status: String, val comentario: String? = null)

// ----- Projetos -----

data class ProjetoDto(
    val id: String,
    val nome: String,
    val descricao: String?,
    val responsavelNome: String?,
    val orientacaoId: String?,
    val ideiaOrigemId: String?,
    val etapa: Int,
    val totalEtapas: Int,
    val progresso: Int,
    val status: String,
    val dataInicio: String?,
    val prazo: String?,
    val investimento: Double?,
    val retornoFinanceiro: Double?,
    val custoEvitado: Double?,
    val aumentoProdutividade: Double?
)

data class ProjetoRequestDto(
    val nome: String,
    val descricao: String?,
    val orientacaoId: String,
    val ideiaOrigemId: String?,
    val totalEtapas: Int,
    val dataInicio: String,
    val prazo: String,
    val investimento: Double
)

data class ProgressoRequestDto(
    val etapa: Int,
    val progresso: Int,
    val status: String?,
    val observacao: String?
)

data class ResultadosRequestDto(
    val retornoFinanceiro: Double,
    val custoEvitado: Double?,
    val aumentoProdutividade: Double?
)

data class PrioridadeRequestDto(val prioridade: String)

// ----- Dashboards -----

data class PainelGestorDto(
    val ideiasNovas: Int,
    val ideiasEmAnalise: Int,
    val ideiasAprovadas: Int,
    val ideiasRejeitadas: Int,
    val projetosAtivos: Int,
    val projetosAtrasados: Int
)

data class ResumoLiderancaDto(
    val indicadores: IndicadoresDto,
    val projetosPorStatus: Map<String, Int>?,
    val ideiasPorStatus: Map<String, Int>?,
    val porOrientacao: List<ResumoOrientacaoDto>?
)

data class ResumoOrientacaoDto(
    val orientacaoId: String,
    val titulo: String,
    val campanha: String?,
    val area: String?,
    val ideias: Int,
    val indicadores: IndicadoresDto
)

data class ResumoProjetoDto(
    val id: String,
    val nome: String,
    val status: String,
    val etapa: Int,
    val totalEtapas: Int,
    val progresso: Int,
    val prazo: String?,
    val orientacaoTitulo: String?,
    val investimento: Double?,
    val retornoFinanceiro: Double?,
    val lucro: Double?,
    val roi: Double?,
    val custoEvitado: Double?,
    val aumentoProdutividade: Double?
)

data class OrientacaoRequestDto(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String,
    val area: String,
    val periodo: String,
    val indicadores: List<String>,
    val vigente: Boolean
)

data class HistoricoDto(
    val data: String?,
    val acao: String?,
    val titulo: String?,
    val categoria: String?,
    val campanha: String?,
    val alteradoPorNome: String?
)

data class IndicadoresDto(
    val totalProjetos: Int,
    val projetosAtivos: Int,
    val projetosNoPrazo: Int,
    val percentualNoPrazo: Double?,
    val investimentoTotal: Double?,
    val retornoTotal: Double?,
    val lucro: Double?,
    val roi: Double?,
    val custoEvitadoTotal: Double?,
    val produtividadeMedia: Double?
)

/** Formato padrão de erro da API. */
data class ErroDto(val status: Int?, val message: String?)
