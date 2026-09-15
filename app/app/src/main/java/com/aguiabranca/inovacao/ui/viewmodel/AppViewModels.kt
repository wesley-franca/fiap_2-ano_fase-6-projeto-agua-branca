package com.aguiabranca.inovacao.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aguiabranca.inovacao.data.model.*
import com.aguiabranca.inovacao.data.repository.MockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ==================== OPERADOR VIEW MODEL ====================
data class OperadorUiState(
    val orientacoes: List<Orientacao> = emptyList(),
    val minhasIdeias: List<Idea> = emptyList(),
    val isLoading: Boolean = false,
    val ultimaOrientacao: Orientacao? = null
)

class OperadorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OperadorUiState())
    val uiState: StateFlow<OperadorUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val orientacoes = MockRepository.getOrientacoes()
            val ideias = MockRepository.getMinhasIdeias()
            
            _uiState.value = _uiState.value.copy(
                orientacoes = orientacoes,
                minhasIdeias = ideias,
                ultimaOrientacao = orientacoes.firstOrNull()
            )
        }
    }

    fun adicionarIdeia(titulo: String, categoria: String, problema: String, proposta: String) {
        val novaIdeia = Idea(
            titulo = titulo,
            categoria = categoria,
            problemaObservado = problema,
            suaProposta = proposta,
            nomeOperador = "Você",
            criadoEm = "agora"
        )
        MockRepository.adicionarIdeia(novaIdeia)
        loadData()
    }
}

// ==================== GESTOR VIEW MODEL ====================
data class GestorUiState(
    val ideiasParaAprovar: List<Idea> = emptyList(),
    val meusProjetos: List<Projeto> = emptyList(),
    val kpisNovas: Int = 0,
    val kpisEmAnalise: Int = 0,
    val kpisAtivos: Int = 0,
    val isLoading: Boolean = false
)

class GestorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GestorUiState())
    val uiState: StateFlow<GestorUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val ideias = MockRepository.getIdeiasParaAprovar()
            val projetos = MockRepository.getMeusProjetos()
            
            val novas = ideias.count { it.status == IdeaStatus.ENVIADA }
            val emAnalise = ideias.count { it.status == IdeaStatus.ANALISE }
            
            _uiState.value = GestorUiState(
                ideiasParaAprovar = ideias,
                meusProjetos = projetos,
                kpisNovas = novas,
                kpisEmAnalise = emAnalise,
                kpisAtivos = projetos.size
            )
        }
    }

    fun aprovarIdeia(ideaId: String) {
        MockRepository.atualizarStatusIdeia(ideaId, IdeaStatus.PROJETO)
        loadData()
    }

    fun rejeitarIdeia(ideaId: String) {
        MockRepository.atualizarStatusIdeia(ideaId, IdeaStatus.REJEITADA)
        loadData()
    }
}

// ==================== LIDERANÇA VIEW MODEL ====================
data class LiderancaUiState(
    val metricas: DashboardMetricas = DashboardMetricas(),
    val orientacoes: List<Orientacao> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val isLoading: Boolean = false
)

class LiderancaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LiderancaUiState())
    val uiState: StateFlow<LiderancaUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val metricas = MockRepository.getDashboardMetricas()
            val orientacoes = MockRepository.getOrientacoes()
            val projetos = MockRepository.getTodosProjetos()
            
            _uiState.value = LiderancaUiState(
                metricas = metricas,
                orientacoes = orientacoes,
                projetos = projetos
            )
        }
    }
}
