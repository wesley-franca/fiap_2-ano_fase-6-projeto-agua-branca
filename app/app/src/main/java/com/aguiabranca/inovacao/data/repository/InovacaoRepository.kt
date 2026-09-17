package com.aguiabranca.inovacao.data.repository

import com.aguiabranca.inovacao.data.model.DashboardMetricas
import com.aguiabranca.inovacao.data.model.Idea
import com.aguiabranca.inovacao.data.model.IdeaStatus
import com.aguiabranca.inovacao.data.model.Orientacao
import com.aguiabranca.inovacao.data.model.Projeto
import com.aguiabranca.inovacao.data.model.User
import com.aguiabranca.inovacao.data.remote.ApiClient
import com.aguiabranca.inovacao.data.remote.IdeiaDto
import com.aguiabranca.inovacao.data.remote.LoginRequestDto
import com.aguiabranca.inovacao.data.remote.NovaIdeiaDto
import com.aguiabranca.inovacao.data.remote.OrientacaoDto
import com.aguiabranca.inovacao.data.remote.PainelGestorDto
import com.aguiabranca.inovacao.data.remote.ProjetoDto
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

    suspend fun projetos(): Result<List<Projeto>> = chamar {
        api.projetos().map { it.paraProjeto() }
    }

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

    // ----- Conversão API -> modelos das telas -----

    private fun UsuarioDto.paraUsuario() = User(
        uid = id,
        email = email,
        nome = nome,
        role = role,
        area = area.orEmpty()
    )

    private fun OrientacaoDto.paraOrientacao(): Orientacao {
        val indicadores = indicadores.orEmpty()
        return Orientacao(
            id = id,
            titulo = titulo,
            descricao = descricao.orEmpty(),
            area = area.orEmpty(),
            periodo = periodo.orEmpty(),
            indicador1 = indicadores.getOrElse(0) { "" },
            indicador2 = indicadores.getOrElse(1) { "" },
            indicador3 = indicadores.getOrElse(2) { "" }
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
        progresso = progresso
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
