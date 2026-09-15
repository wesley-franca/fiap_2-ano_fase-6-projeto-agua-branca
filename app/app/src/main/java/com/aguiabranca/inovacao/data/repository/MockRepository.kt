package com.aguiabranca.inovacao.data.repository

import com.aguiabranca.inovacao.data.model.*
import kotlinx.coroutines.delay

object MockRepository {
    private var currentUser: User? = null
    private var ideas = mutableListOf<Idea>()
    private var projetos = mutableListOf<Projeto>()
    
    init {
        // Dados de exemplo
        ideas.addAll(
            listOf(
                Idea(
                    id = "1",
                    titulo = "Sensor de vibração na linha 3",
                    categoria = "Manutenção",
                    problemaObservado = "Máquina apresenta vibrações anormais",
                    suaProposta = "Instalar sensor para monitoramento em tempo real",
                    status = IdeaStatus.ANALISE,
                    prioridade = IdeaPriority.ALTA,
                    nomeOperador = "João Costa",
                    criadoEm = "há 3 dias"
                ),
                Idea(
                    id = "2",
                    titulo = "Checklist digital de turno",
                    categoria = "Operação",
                    problemaObservado = "Checklist em papel gera atrasos",
                    suaProposta = "App mobile para checklist com sincronização",
                    status = IdeaStatus.ENVIADA,
                    prioridade = IdeaPriority.MEDIA,
                    nomeOperador = "Ana Lima",
                    criadoEm = "há 5 dias"
                ),
                Idea(
                    id = "3",
                    titulo = "Reaproveitar embalagens da L2",
                    categoria = "Sustentabilidade",
                    problemaObservado = "Muita embalagem desperdiçada",
                    suaProposta = "Programa de retorno de embalagens",
                    status = IdeaStatus.PROJETO,
                    prioridade = IdeaPriority.MEDIA,
                    nomeOperador = "Roberto Mendes",
                    criadoEm = "há 2 semanas"
                )
            )
        )
        
        projetos.addAll(
            listOf(
                Projeto(
                    id = "1",
                    nome = "Padronização de setup",
                    responsavel = "M. Silva",
                    etapa = 2,
                    totalEtapas = 4,
                    status = "No prazo",
                    prazo = "12 jun",
                    investimento = "R$ 84k",
                    progresso = 48
                ),
                Projeto(
                    id = "2",
                    nome = "Sensor de vibração L3",
                    responsavel = "M. Silva",
                    etapa = 1,
                    totalEtapas = 4,
                    status = "No prazo",
                    prazo = "12 jun",
                    investimento = "R$ 110k",
                    progresso = 25
                ),
                Projeto(
                    id = "3",
                    nome = "Checklist digital de turno",
                    responsavel = "C. Santos",
                    etapa = 3,
                    totalEtapas = 4,
                    status = "Atrasado",
                    prazo = "12 jun",
                    investimento = "R$ 22k",
                    progresso = 60
                )
            )
        )
    }
    
    suspend fun login(email: String, password: String): Result<User> {
        delay(500) // Simula delay de rede
        return when {
            email == "operador@aguiabranca.com" && password == "senha123" -> {
                currentUser = User(
                    uid = "op1",
                    email = email,
                    nome = "João Operador",
                    role = UserRole.OPERADOR,
                    area = "Manutenção"
                )
                Result.success(currentUser!!)
            }
            email == "gestor@aguiabranca.com" && password == "senha123" -> {
                currentUser = User(
                    uid = "ges1",
                    email = email,
                    nome = "Maria Gestora",
                    role = UserRole.GESTOR,
                    area = "Manutenção"
                )
                Result.success(currentUser!!)
            }
            email == "lideranca@aguiabranca.com" && password == "senha123" -> {
                currentUser = User(
                    uid = "lider1",
                    email = email,
                    nome = "Paulo Liderança",
                    role = UserRole.LIDERANCA,
                    area = "Geral"
                )
                Result.success(currentUser!!)
            }
            else -> Result.failure(Exception("Email ou senha inválidos"))
        }
    }
    
    fun logout() {
        currentUser = null
    }
    
    fun getCurrentUser(): User? = currentUser
    
    fun getOrientacoes(): List<Orientacao> {
        return listOf(
            Orientacao(
                id = "1",
                titulo = "Reduzir paradas em 15%",
                descricao = "Foco do trimestre: reduzir paradas não planejadas em 15%",
                area = "Manutenção",
                periodo = "Q2 2026",
                indicador1 = "OEE: 78%",
                indicador2 = "MTBF: 42h",
                indicador3 = "Paradas: -15%"
            ),
            Orientacao(
                id = "2",
                titulo = "Aumentar OEE para 82%",
                descricao = "Meta anual: melhorar Overall Equipment Effectiveness",
                area = "Operação",
                periodo = "Anual 2026",
                indicador1 = "OEE atual: 78%",
                indicador2 = "Meta: 82%",
                indicador3 = "Investimento: R$ 200k"
            ),
            Orientacao(
                id = "3",
                titulo = "Zero acidentes",
                descricao = "Objetivo contínuo de segurança",
                area = "EHS",
                periodo = "Contínuo",
                indicador1 = "Acidentes este ano: 0",
                indicador2 = "Treinamentos: 100%",
                indicador3 = "NR10 atualizado: Sim"
            )
        )
    }
    
    fun getMinhasIdeias(): List<Idea> = ideas.filter { it.nomeOperador.isNotEmpty() }
    
    fun getIdeiasParaAprovar(): List<Idea> {
        return ideas.filter { it.status == IdeaStatus.ENVIADA || it.status == IdeaStatus.ANALISE }
    }
    
    fun getMeusProjetos(): List<Projeto> = projetos
    
    fun getTodosProjetos(): List<Projeto> = projetos
    
    fun getDashboardMetricas(): DashboardMetricas {
        return DashboardMetricas(
            roi = "2,4x",
            lucro = "R$ 1,9M",
            projetosAtivos = 11,
            noPrazo = 9,
            custoEvitado = "R$ 420k",
            produtividade = "+9,4%"
        )
    }
    
    fun adicionarIdeia(idea: Idea) {
        ideas.add(idea.copy(id = (ideas.size + 1).toString()))
    }
    
    fun atualizarStatusIdeia(ideaId: String, novoStatus: IdeaStatus) {
        val index = ideas.indexOfFirst { it.id == ideaId }
        if (index >= 0) {
            ideas[index] = ideas[index].copy(status = novoStatus)
        }
    }
}
