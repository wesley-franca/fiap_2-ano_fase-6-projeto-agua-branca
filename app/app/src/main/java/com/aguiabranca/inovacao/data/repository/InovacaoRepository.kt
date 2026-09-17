package com.aguiabranca.inovacao.data.repository

import com.aguiabranca.inovacao.data.model.DashboardMetricas
import com.aguiabranca.inovacao.data.model.HistoricoOrientacao
import com.aguiabranca.inovacao.data.model.Idea
import com.aguiabranca.inovacao.data.model.IdeaPriority
import com.aguiabranca.inovacao.data.model.IdeaStatus
import com.aguiabranca.inovacao.data.model.Orientacao
import com.aguiabranca.inovacao.data.model.Projeto
import com.aguiabranca.inovacao.data.model.ResumoOrientacao
import com.aguiabranca.inovacao.data.model.ResumoProjeto
import com.aguiabranca.inovacao.data.model.User
import com.aguiabranca.inovacao.data.remote.ApiClient
import com.aguiabranca.inovacao.data.remote.IdeiaDto
import com.aguiabranca.inovacao.data.remote.LoginRequestDto
import com.aguiabranca.inovacao.data.remote.NovaIdeiaDto
import com.aguiabranca.inovacao.data.remote.OrientacaoDto
import com.aguiabranca.inovacao.data.remote.OrientacaoRequestDto
import com.aguiabranca.inovacao.data.remote.PainelGestorDto
import com.aguiabranca.inovacao.data.remote.PrioridadeRequestDto
import com.aguiabranca.inovacao.data.remote.ProgressoRequestDto
import com.aguiabranca.inovacao.data.remote.ProjetoDto
import com.aguiabranca.inovacao.data.remote.ProjetoRequestDto
import com.aguiabranca.inovacao.data.remote.ResultadosRequestDto
import com.aguiabranca.inovacao.data.remote.StatusIdeiaDto
import com.aguiabranca.inovacao.data.remote.UsuarioDto
import com.aguiabranca.inovacao.data.session.Sessao
import com.aguiabranca.inovacao.util.Formatadores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** Única fonte de dados do app: todas as telas passam por aqui e tudo vem da API. */
object InovacaoRepository {

    private val api = ApiClient.api

    suspend fun login(email: String, senha: String): Result<User> = chamar {
        val resposta = api.login(LoginRequestDto(email.trim(), senha))
        val usuario = resposta.usuario.paraUsuario()
        Sessao.iniciar(resposta.token, usuario)
        usuario
    }

    suspend fun usuarioAtual(): Result<User> = chamar { api.me().paraUsuario() }

    /**
     * Recupera a sessão salva no disco e confirma o token com a API.
     * Retorna null quando não há sessão salva ou quando o token não vale mais.
     */
    suspend fun restaurarSessao(): User? {
        Sessao.restaurar() ?: return null
        return usuarioAtual()
            .onSuccess { Sessao.atualizarUsuario(it) }
            .getOrNull()
    }

    suspend fun logout() = Sessao.encerrar()

    suspend fun orientacoes(apenasVigentes: Boolean = false): Result<List<Orientacao>> = chamar {
        api.orientacoes(if (apenasVigentes) true else null).map { it.paraOrientacao() }
    }

    suspend fun historicoOrientacao(id: String): Result<List<HistoricoOrientacao>> = chamar {
        api.historicoOrientacao(id).map {
            HistoricoOrientacao(
                data = Formatadores.tempoRelativo(it.data),
                acao = it.acao.orEmpty(),
                titulo = it.titulo.orEmpty(),
                categoria = it.categoria.orEmpty(),
                campanha = it.campanha.orEmpty(),
                autor = it.alteradoPorNome.orEmpty()
            )
        }
    }

    suspend fun salvarOrientacao(
        id: String?,
        titulo: String,
        descricao: String,
        categoria: String,
        campanha: String,
        area: String,
        periodo: String,
        indicadores: List<String>,
        vigente: Boolean
    ): Result<Orientacao> = chamar {
        val corpo = OrientacaoRequestDto(
            titulo = titulo,
            descricao = descricao,
            categoria = categoria,
            campanha = campanha,
            area = area,
            periodo = periodo,
            indicadores = indicadores.filter { it.isNotBlank() },
            vigente = vigente
        )
        val resposta = if (id == null) api.criarOrientacao(corpo) else api.atualizarOrientacao(id, corpo)
        resposta.paraOrientacao()
    }

    suspend fun excluirOrientacao(id: String): Result<Unit> = chamar { api.excluirOrientacao(id) }

    suspend fun minhasIdeias(): Result<List<Idea>> = chamar {
        api.ideias().map { it.paraIdeia() }
    }

    /** Fila do gestor: tudo que ainda depende de avaliação. */
    suspend fun ideiasParaAvaliar(): Result<List<Idea>> = chamar {
        api.ideias()
            .filter { it.status != IdeaStatus.PROJETO && it.status != IdeaStatus.REJEITADA }
            .map { it.paraIdeia() }
    }

    suspend fun criarIdeia(
        titulo: String,
        categoria: String,
        problema: String,
        proposta: String,
        orientacaoId: String
    ): Result<Idea> = chamar {
        api.criarIdeia(NovaIdeiaDto(titulo, categoria, problema, proposta, orientacaoId)).paraIdeia()
    }

    suspend fun buscarIdeia(id: String): Result<Idea> = chamar { api.ideia(id).paraIdeia() }

    suspend fun atualizarIdeia(
        id: String,
        titulo: String,
        categoria: String,
        problema: String,
        proposta: String,
        orientacaoId: String
    ): Result<Idea> = chamar {
        api.atualizarIdeia(id, NovaIdeiaDto(titulo, categoria, problema, proposta, orientacaoId)).paraIdeia()
    }

    suspend fun excluirIdeia(id: String): Result<Unit> = chamar { api.excluirIdeia(id) }

    /**
     * Avança a ideia uma etapa no fluxo da API (ENVIADA → ANALISE → DECISAO → PROJETO).
     * O botão "Aprovar" da fila do gestor usa este caminho.
     */
    suspend fun avancarIdeia(id: String, statusAtual: IdeaStatus): Result<Idea> {
        val proximo = when (statusAtual) {
            IdeaStatus.ENVIADA, IdeaStatus.TRIAGEM -> IdeaStatus.ANALISE
            IdeaStatus.ANALISE -> IdeaStatus.DECISAO
            IdeaStatus.DECISAO -> IdeaStatus.PROJETO
            else -> return Result.failure(IllegalStateException("Esta ideia já foi concluída"))
        }
        return chamar { api.alterarStatusIdeia(id, StatusIdeiaDto(proximo.name)).paraIdeia() }
    }

    suspend fun rejeitarIdeia(id: String, motivo: String): Result<Idea> = chamar {
        api.alterarStatusIdeia(id, StatusIdeiaDto(IdeaStatus.REJEITADA.name, motivo)).paraIdeia()
    }

    suspend fun priorizarIdeia(id: String, prioridade: IdeaPriority): Result<Idea> = chamar {
        api.priorizarIdeia(id, PrioridadeRequestDto(prioridade.name)).paraIdeia()
    }

    suspend fun projetos(): Result<List<Projeto>> = chamar {
        api.projetos().map { it.paraProjeto() }
    }

    suspend fun salvarProjeto(
        id: String?,
        nome: String,
        descricao: String,
        orientacaoId: String,
        ideiaOrigemId: String?,
        totalEtapas: Int,
        dataInicio: String,
        prazo: String,
        investimento: Double
    ): Result<Projeto> = chamar {
        val corpo = ProjetoRequestDto(
            nome = nome,
            descricao = descricao,
            orientacaoId = orientacaoId,
            ideiaOrigemId = ideiaOrigemId?.takeIf { it.isNotBlank() },
            totalEtapas = totalEtapas,
            dataInicio = dataInicio,
            prazo = prazo,
            investimento = investimento
        )
        val resposta = if (id == null) api.criarProjeto(corpo) else api.atualizarProjeto(id, corpo)
        resposta.paraProjeto()
    }

    suspend fun registrarProgresso(
        id: String,
        etapa: Int,
        progresso: Int,
        status: String?,
        observacao: String?
    ): Result<Projeto> = chamar {
        api.registrarProgresso(id, ProgressoRequestDto(etapa, progresso, status, observacao)).paraProjeto()
    }

    suspend fun registrarResultados(
        id: String,
        retorno: Double,
        custoEvitado: Double?,
        produtividade: Double?
    ): Result<Projeto> = chamar {
        api.registrarResultados(id, ResultadosRequestDto(retorno, custoEvitado, produtividade)).paraProjeto()
    }

    suspend fun excluirProjeto(id: String): Result<Unit> = chamar { api.excluirProjeto(id) }

    suspend fun painelGestor(): Result<PainelGestorDto> = chamar { api.painelGestor() }

    suspend fun metricasDaLideranca(): Result<DashboardMetricas> = chamar {
        val i = api.resumoLideranca().indicadores
        DashboardMetricas(
            roi = Formatadores.multiplicador(i.roi),
            lucro = Formatadores.moeda(i.lucro),
            projetosAtivos = i.projetosAtivos,
            noPrazo = i.projetosNoPrazo,
            custoEvitado = Formatadores.moeda(i.custoEvitadoTotal),
            produtividade = Formatadores.percentualComSinal(i.produtividadeMedia)
        )
    }

    /** Resumo completo: indicadores para os KPIs e séries para os gráficos. */
    suspend fun resumoDaLideranca(): Result<ResumoDashboard> = chamar {
        val resposta = api.resumoLideranca()
        val i = resposta.indicadores
        ResumoDashboard(
            metricas = DashboardMetricas(
                roi = Formatadores.multiplicador(i.roi),
                lucro = Formatadores.moeda(i.lucro),
                projetosAtivos = i.projetosAtivos,
                noPrazo = i.projetosNoPrazo,
                custoEvitado = Formatadores.moeda(i.custoEvitadoTotal),
                produtividade = Formatadores.percentualComSinal(i.produtividadeMedia)
            ),
            projetosPorStatus = resposta.projetosPorStatus.orEmpty(),
            ideiasPorStatus = resposta.ideiasPorStatus.orEmpty(),
            porOrientacao = resposta.porOrientacao.orEmpty().map { o ->
                ResumoOrientacao(
                    orientacaoId = o.orientacaoId,
                    titulo = o.titulo,
                    campanha = o.campanha.orEmpty(),
                    ideias = o.ideias,
                    projetos = o.indicadores.totalProjetos,
                    lucro = o.indicadores.lucro ?: 0.0,
                    investimento = o.indicadores.investimentoTotal ?: 0.0,
                    retorno = o.indicadores.retornoTotal ?: 0.0,
                    roi = o.indicadores.roi
                )
            }
        )
    }

    suspend fun resumoDoProjeto(id: String): Result<ResumoProjeto> = chamar {
        val p = api.resumoProjeto(id)
        ResumoProjeto(
            id = p.id,
            nome = p.nome,
            status = Formatadores.statusProjeto(p.status),
            etapa = p.etapa,
            totalEtapas = p.totalEtapas,
            progresso = p.progresso,
            prazo = Formatadores.dataCurta(p.prazo),
            orientacaoTitulo = p.orientacaoTitulo.orEmpty(),
            investimento = Formatadores.moeda(p.investimento),
            retorno = Formatadores.moeda(p.retornoFinanceiro),
            lucro = Formatadores.moeda(p.lucro),
            roi = Formatadores.multiplicador(p.roi),
            custoEvitado = Formatadores.moeda(p.custoEvitado),
            produtividade = Formatadores.percentualComSinal(p.aumentoProdutividade)
        )
    }

    data class ResumoDashboard(
        val metricas: DashboardMetricas,
        val projetosPorStatus: Map<String, Int>,
        val ideiasPorStatus: Map<String, Int>,
        val porOrientacao: List<ResumoOrientacao>
    )

    // ----- Conversão API -> modelos das telas -----

    private fun UsuarioDto.paraUsuario() = User(
        uid = id,
        email = email,
        nome = nome,
        role = role,
        area = area.orEmpty()
    )

    private fun OrientacaoDto.paraOrientacao(): Orientacao {
        val lista = indicadores.orEmpty()
        return Orientacao(
            id = id,
            titulo = titulo,
            descricao = descricao.orEmpty(),
            area = area.orEmpty(),
            periodo = periodo.orEmpty(),
            indicador1 = lista.getOrElse(0) { "" },
            indicador2 = lista.getOrElse(1) { "" },
            indicador3 = lista.getOrElse(2) { "" },
            categoria = categoria.orEmpty(),
            campanha = campanha.orEmpty(),
            indicadores = lista,
            vigente = vigente
        )
    }

    private fun IdeiaDto.paraIdeia() = Idea(
        id = id,
        titulo = titulo,
        categoria = categoria.orEmpty(),
        problemaObservado = problemaObservado.orEmpty(),
        suaProposta = suaProposta.orEmpty(),
        status = status,
        prioridade = prioridade,
        nomeOperador = nomeOperador.orEmpty(),
        criadoEm = Formatadores.tempoRelativo(criadoEm),
        impacto = impacto.orEmpty(),
        area = area.orEmpty(),
        orientacaoId = orientacaoId.orEmpty(),
        comentarioAvaliacao = comentarioAvaliacao.orEmpty()
    )

    private fun ProjetoDto.paraProjeto() = Projeto(
        id = id,
        nome = nome,
        responsavel = responsavelNome.orEmpty(),
        etapa = etapa,
        totalEtapas = totalEtapas,
        status = Formatadores.statusProjeto(status),
        prazo = Formatadores.dataCurta(prazo),
        investimento = Formatadores.moeda(investimento),
        progresso = progresso,
        descricao = descricao.orEmpty(),
        orientacaoId = orientacaoId.orEmpty(),
        ideiaOrigemId = ideiaOrigemId.orEmpty(),
        statusApi = status,
        dataInicioIso = dataInicio.orEmpty(),
        prazoIso = prazo.orEmpty(),
        investimentoValor = investimento ?: 0.0,
        retornoValor = retornoFinanceiro,
        custoEvitadoValor = custoEvitado,
        produtividadeValor = aumentoProdutividade
    )

    /** Converte falhas de rede e respostas de erro da API em mensagens legíveis. */
    private suspend fun <T> chamar(bloco: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(bloco())
        } catch (e: HttpException) {
            val mensagem = ApiClient.mensagemDeErro(e.response()?.errorBody()?.string())
            if (e.code() == 401) {
                Sessao.expirar()
            }
            Result.failure(Exception(mensagem ?: mensagemPadrao(e.code())))
        } catch (e: IOException) {
            Result.failure(Exception("Sem conexão com o servidor. Verifique se a API está no ar."))
        }
    }

    private fun mensagemPadrao(codigo: Int) = when (codigo) {
        401 -> "Sessão expirada. Entre novamente."
        403 -> "Seu perfil não tem permissão para esta operação."
        404 -> "Registro não encontrado."
        else -> "Não foi possível completar a operação (erro $codigo)."
    }
}
