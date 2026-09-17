package com.aguiabranca.inovacao.data.model

import java.util.Date

// Enums
enum class UserRole {
    OPERADOR, GESTOR, LIDERANCA
}

enum class IdeaStatus {
    ENVIADA, TRIAGEM, ANALISE, DECISAO, PROJETO, REJEITADA
}

enum class IdeaPriority {
    BAIXA, MEDIA, ALTA
}

// Models
data class User(
    val uid: String = "",
    val email: String = "",
    val nome: String = "",
    val role: UserRole = UserRole.OPERADOR,
    val area: String = ""
)

data class Orientacao(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val area: String = "",
    val periodo: String = "",
    val indicador1: String = "",
    val indicador2: String = "",
    val indicador3: String = "",
    val categoria: String = "",
    val campanha: String = "",
    val indicadores: List<String> = emptyList(),
    val vigente: Boolean = false
)

/** Um registro do histórico de alterações de uma orientação. */
data class HistoricoOrientacao(
    val data: String = "",
    val acao: String = "",
    val titulo: String = "",
    val categoria: String = "",
    val campanha: String = "",
    val autor: String = ""
)

/** Resultado consolidado de uma orientação, usado nos gráficos da liderança. */
data class ResumoOrientacao(
    val orientacaoId: String = "",
    val titulo: String = "",
    val campanha: String = "",
    val ideias: Int = 0,
    val projetos: Int = 0,
    val lucro: Double = 0.0,
    val investimento: Double = 0.0,
    val retorno: Double = 0.0,
    val roi: Double? = null
)

/** Retorno detalhado de um projeto para a liderança. */
data class ResumoProjeto(
    val id: String = "",
    val nome: String = "",
    val status: String = "",
    val etapa: Int = 0,
    val totalEtapas: Int = 0,
    val progresso: Int = 0,
    val prazo: String = "",
    val orientacaoTitulo: String = "",
    val investimento: String = "",
    val retorno: String = "",
    val lucro: String = "",
    val roi: String = "",
    val custoEvitado: String = "",
    val produtividade: String = ""
)

data class Idea(
    val id: String = "",
    val titulo: String = "",
    val categoria: String = "",
    val problemaObservado: String = "",
    val suaProposta: String = "",
    val status: IdeaStatus = IdeaStatus.ENVIADA,
    val prioridade: IdeaPriority = IdeaPriority.MEDIA,
    val nomeOperador: String = "",
    val criadoEm: String = "",
    val impacto: String = "Médio",
    val area: String = "",
    val orientacaoId: String = "",
    val comentarioAvaliacao: String = ""
)

data class Projeto(
    val id: String = "",
    val nome: String = "",
    val responsavel: String = "",
    val etapa: Int = 1,
    val totalEtapas: Int = 4,
    val status: String = "No prazo",
    val prazo: String = "",
    val investimento: String = "",
    val progresso: Int = 0,
    // Campos crus da API, usados nos formulários de edição.
    val descricao: String = "",
    val orientacaoId: String = "",
    val ideiaOrigemId: String = "",
    val statusApi: String = "NO_PRAZO",
    val dataInicioIso: String = "",
    val prazoIso: String = "",
    val investimentoValor: Double = 0.0,
    val retornoValor: Double? = null,
    val custoEvitadoValor: Double? = null,
    val produtividadeValor: Double? = null
)

data class DashboardMetricas(
    val roi: String = "2,4x",
    val lucro: String = "R$ 1,9M",
    val projetosAtivos: Int = 11,
    val noPrazo: Int = 9,
    val custoEvitado: String = "R$ 420k",
    val produtividade: String = "+9,4%"
)
