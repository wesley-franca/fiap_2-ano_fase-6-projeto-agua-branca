package com.aguiabranca.inovacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguiabranca.inovacao.data.model.DashboardMetricas
import com.aguiabranca.inovacao.data.model.Idea
import com.aguiabranca.inovacao.data.model.IdeaPriority
import com.aguiabranca.inovacao.data.model.IdeaStatus
import com.aguiabranca.inovacao.data.model.Orientacao
import com.aguiabranca.inovacao.data.model.Projeto
import com.aguiabranca.inovacao.data.repository.InovacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ==================== OPERADOR ====================
data class OperadorUiState(
    val orientacoes: List<Orientacao> = emptyList(),
    val minhasIdeias: List<Idea> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val ultimaOrientacao: Orientacao? = null,
    /** Vira `true` quando um envio, edição ou exclusão termina, para a tela poder voltar. */
    val operacaoConcluida: Boolean = false
)

class OperadorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OperadorUiState())
    val uiState: StateFlow<OperadorUiState> = _uiState

    init {
        carregar()
    }

    fun carregar() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val orientacoes = InovacaoRepository.orientacoes(apenasVigentes = true)
            val ideias = InovacaoRepository.minhasIdeias()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                orientacoes = orientacoes.getOrDefault(emptyList()),
                ultimaOrientacao = orientacoes.getOrNull()?.firstOrNull(),
                minhasIdeias = ideias.getOrDefault(emptyList()),
                errorMessage = primeiroErro(listOf(orientacoes, ideias))
            )
        }
    }

    fun ideia(id: String): Idea? = _uiState.value.minhasIdeias.firstOrNull { it.id == id }

    fun adicionarIdeia(
        titulo: String,
        categoria: String,
        problema: String,
        proposta: String,
        orientacaoId: String
    ) = executar {
        InovacaoRepository.criarIdeia(titulo, categoria, problema, proposta, orientacaoId)
    }

    fun atualizarIdeia(
        id: String,
        titulo: String,
        categoria: String,
        problema: String,
        proposta: String,
        orientacaoId: String
    ) = executar {
        InovacaoRepository.atualizarIdeia(id, titulo, categoria, problema, proposta, orientacaoId)
    }

    fun excluirIdeia(id: String) = executar { InovacaoRepository.excluirIdeia(id) }

    fun operacaoTratada() {
        _uiState.value = _uiState.value.copy(operacaoConcluida = false)
    }

    private fun executar(acao: suspend () -> Result<*>) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, operacaoConcluida = false)
        viewModelScope.launch {
            acao()
                .onSuccess {
                    carregar()
                    _uiState.value = _uiState.value.copy(operacaoConcluida = true)
                }
                .onFailure { erro ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = erro.message)
                }
        }
    }
}

// ==================== GESTOR ====================
enum class FiltroIdeia(val rotulo: String) {
    TODAS("Todas"),
    NOVAS("Novas"),
    EM_ANALISE("Em análise"),
    ALTA_PRIORIDADE("Prioridade alta")
}

data class GestorUiState(
    val ideiasParaAprovar: List<Idea> = emptyList(),
    val meusProjetos: List<Projeto> = emptyList(),
    val orientacoes: List<Orientacao> = emptyList(),
    val filtro: FiltroIdeia = FiltroIdeia.TODAS,
    val areaSelecionada: String? = null,
    val kpisNovas: Int = 0,
    val kpisEmAnalise: Int = 0,
    val kpisAtivos: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val operacaoConcluida: Boolean = false
) {
    /** Áreas presentes na fila, para o filtro "Por área". */
    val areas: List<String> get() = ideiasParaAprovar.mapNotNull { it.area.ifBlank { null } }.distinct().sorted()

    val ideiasFiltradas: List<Idea>
        get() = ideiasParaAprovar
            .filter { ideia ->
                when (filtro) {
                    FiltroIdeia.TODAS -> true
                    FiltroIdeia.NOVAS -> ideia.status == IdeaStatus.ENVIADA
                    FiltroIdeia.EM_ANALISE -> ideia.status == IdeaStatus.TRIAGEM ||
                            ideia.status == IdeaStatus.ANALISE ||
                            ideia.status == IdeaStatus.DECISAO
                    FiltroIdeia.ALTA_PRIORIDADE -> ideia.prioridade == IdeaPriority.ALTA
                }
            }
            .filter { areaSelecionada == null || it.area.equals(areaSelecionada, ignoreCase = true) }
}

class GestorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GestorUiState())
    val uiState: StateFlow<GestorUiState> = _uiState

    init {
        carregar()
    }

    fun carregar() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val ideias = InovacaoRepository.ideiasParaAvaliar()
            val projetos = InovacaoRepository.projetos()
            val painel = InovacaoRepository.painelGestor()
            val orientacoes = InovacaoRepository.orientacoes()

            _uiState.value = _uiState.value.copy(
                ideiasParaAprovar = ideias.getOrDefault(emptyList()),
                meusProjetos = projetos.getOrDefault(emptyList()),
                orientacoes = orientacoes.getOrDefault(emptyList()),
                kpisNovas = painel.getOrNull()?.ideiasNovas ?: 0,
                kpisEmAnalise = painel.getOrNull()?.ideiasEmAnalise ?: 0,
                kpisAtivos = painel.getOrNull()?.projetosAtivos ?: 0,
                isLoading = false,
                errorMessage = primeiroErro(listOf(ideias, projetos, painel, orientacoes))
            )
        }
    }

    fun ideia(id: String): Idea? = _uiState.value.ideiasParaAprovar.firstOrNull { it.id == id }

    fun projeto(id: String): Projeto? = _uiState.value.meusProjetos.firstOrNull { it.id == id }

    fun aplicarFiltro(filtro: FiltroIdeia) {
        _uiState.value = _uiState.value.copy(filtro = filtro)
    }

    fun filtrarPorArea(area: String?) {
        _uiState.value = _uiState.value.copy(areaSelecionada = area)
    }

    fun aprovarIdeia(ideaId: String) {
        val atual = ideia(ideaId) ?: return
        executar { InovacaoRepository.avancarIdeia(ideaId, atual.status) }
    }

    fun rejeitarIdeia(ideaId: String, motivo: String) {
        executar { InovacaoRepository.rejeitarIdeia(ideaId, motivo) }
    }

    fun priorizarIdeia(ideaId: String, prioridade: IdeaPriority) {
        executar { InovacaoRepository.priorizarIdeia(ideaId, prioridade) }
    }

    fun salvarProjeto(
        id: String?,
        nome: String,
        descricao: String,
        orientacaoId: String,
        ideiaOrigemId: String?,
        totalEtapas: Int,
        dataInicio: String,
        prazo: String,
        investimento: Double
    ) = executar {
        InovacaoRepository.salvarProjeto(
            id, nome, descricao, orientacaoId, ideiaOrigemId, totalEtapas, dataInicio, prazo, investimento
        )
    }

    fun registrarProgresso(id: String, etapa: Int, progresso: Int, status: String?, observacao: String?) =
        executar { InovacaoRepository.registrarProgresso(id, etapa, progresso, status, observacao) }

    fun registrarResultados(id: String, retorno: Double, custoEvitado: Double?, produtividade: Double?) =
        executar { InovacaoRepository.registrarResultados(id, retorno, custoEvitado, produtividade) }

    fun excluirProjeto(id: String) = executar { InovacaoRepository.excluirProjeto(id) }

    fun operacaoTratada() {
        _uiState.value = _uiState.value.copy(operacaoConcluida = false)
    }

    private fun executar(acao: suspend () -> Result<*>) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, operacaoConcluida = false)
        viewModelScope.launch {
            acao()
                .onSuccess {
                    carregar()
                    _uiState.value = _uiState.value.copy(operacaoConcluida = true)
                }
                .onFailure { erro ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = erro.message)
                }
        }
    }
}

// ==================== LIDERANÇA ====================
data class LiderancaUiState(
    val metricas: DashboardMetricas = DashboardMetricas(),
    val orientacoes: List<Orientacao> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LiderancaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LiderancaUiState())
    val uiState: StateFlow<LiderancaUiState> = _uiState

    init {
        carregar()
    }

    fun carregar() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val metricas = InovacaoRepository.metricasDaLideranca()
            val orientacoes = InovacaoRepository.orientacoes()
            val projetos = InovacaoRepository.projetos()

            _uiState.value = LiderancaUiState(
                metricas = metricas.getOrDefault(DashboardMetricas()),
                orientacoes = orientacoes.getOrDefault(emptyList()),
                projetos = projetos.getOrDefault(emptyList()),
                isLoading = false,
                errorMessage = primeiroErro(listOf(metricas, orientacoes, projetos))
            )
        }
    }
}

/** Uma ideia só pode ser alterada pelo autor enquanto ninguém a avaliou. */
fun Idea.podeSerEditada(): Boolean = status == IdeaStatus.ENVIADA

private fun primeiroErro(resultados: List<Result<*>>): String? =
    resultados.firstOrNull { it.isFailure }?.exceptionOrNull()?.message
