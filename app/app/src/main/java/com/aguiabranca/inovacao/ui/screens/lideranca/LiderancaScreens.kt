@file:OptIn(ExperimentalMaterial3Api::class)

package com.aguiabranca.inovacao.ui.screens.lideranca

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aguiabranca.inovacao.data.model.Orientacao
import com.aguiabranca.inovacao.ui.components.Carregando
import com.aguiabranca.inovacao.ui.components.FatiaGrafico
import com.aguiabranca.inovacao.ui.components.GraficoBarras
import com.aguiabranca.inovacao.ui.components.GraficoDistribuicao
import com.aguiabranca.inovacao.ui.components.ListaVazia
import com.aguiabranca.inovacao.ui.components.MensagemDeErro
import com.aguiabranca.inovacao.ui.viewmodel.LiderancaViewModel
import com.aguiabranca.inovacao.util.Formatadores

private val PrimaryBlue = Color(0xFF4C63DD)
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFFA500)
private val NeutralGray = Color(0xFF9E9E9E)

@Composable
fun LiderancaDashboardScreen(
    viewModel: LiderancaViewModel,
    onAbrirOrientacoes: () -> Unit,
    onAbrirProjeto: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liderança - Dashboard") },
                actions = {
                    IconButton(onClick = onAbrirOrientacoes) {
                        Icon(Icons.Filled.Flag, contentDescription = "Orientações")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sair")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Visão geral", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Consolidado da companhia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            uiState.errorMessage?.let { erro ->
                MensagemDeErro(mensagem = erro, onTentarNovamente = viewModel::carregar)
            }

            if (uiState.isLoading && uiState.projetos.isEmpty()) {
                Carregando()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard("ROI\nCONSOLIDADO", uiState.metricas.roi, Modifier.weight(1f))
                KPIDashboard("LUCRO", uiState.metricas.lucro, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard("PROJETOS\nATIVOS", uiState.metricas.projetosAtivos.toString(), Modifier.weight(1f))
                KPIDashboard(
                    "NO PRAZO",
                    "${uiState.metricas.noPrazo}/${uiState.metricas.projetosAtivos}",
                    Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard("CUSTO\nEVITADO", uiState.metricas.custoEvitado, Modifier.weight(1f))
                KPIDashboard("PRODUTIVIDADE", uiState.metricas.produtividade, Modifier.weight(1f))
            }

            SecaoGrafico(titulo = "Lucro por orientação estratégica") {
                GraficoBarras(
                    fatias = uiState.porOrientacao.map { resumo ->
                        FatiaGrafico(
                            rotulo = resumo.titulo,
                            valor = resumo.lucro,
                            cor = PrimaryBlue,
                            valorFormatado = Formatadores.moeda(resumo.lucro)
                        )
                    }
                )
            }

            SecaoGrafico(titulo = "Projetos por situação") {
                GraficoDistribuicao(
                    fatias = listOf(
                        fatia("No prazo", uiState.projetosPorStatus["NO_PRAZO"], SuccessGreen),
                        fatia("Atrasados", uiState.projetosPorStatus["ATRASADO"], MaterialTheme.colorScheme.error),
                        fatia("Concluídos", uiState.projetosPorStatus["CONCLUIDO"], PrimaryBlue),
                        fatia("Cancelados", uiState.projetosPorStatus["CANCELADO"], NeutralGray)
                    )
                )
            }

            SecaoGrafico(titulo = "Ideias por etapa") {
                GraficoDistribuicao(
                    fatias = listOf(
                        fatia("Enviadas", uiState.ideiasPorStatus["ENVIADA"], PrimaryBlue),
                        fatia("Em triagem", uiState.ideiasPorStatus["TRIAGEM"], WarningOrange),
                        fatia("Em análise", uiState.ideiasPorStatus["ANALISE"], WarningOrange.copy(alpha = 0.7f)),
                        fatia("Em decisão", uiState.ideiasPorStatus["DECISAO"], WarningOrange.copy(alpha = 0.5f)),
                        fatia("Viraram projeto", uiState.ideiasPorStatus["PROJETO"], SuccessGreen),
                        fatia("Rejeitadas", uiState.ideiasPorStatus["REJEITADA"], NeutralGray)
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = "Orientações estratégicas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.porOrientacao.forEach { resumo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = resumo.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${resumo.campanha} · ${resumo.projetos} projetos · ${resumo.ideias} ideias",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Lucro ${Formatadores.moeda(resumo.lucro)} · ROI ${Formatadores.multiplicador(resumo.roi)}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = "Andamento de projetos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (uiState.projetos.isEmpty() && !uiState.isLoading) {
                ListaVazia("Nenhum projeto em andamento")
            }

            uiState.projetos.forEach { projeto ->
                Card(
                    onClick = { onAbrirProjeto(projeto.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = projeto.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Etapa ${projeto.etapa}/${projeto.totalEtapas}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (projeto.statusApi == "ATRASADO") {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                } else {
                                    SuccessGreen.copy(alpha = 0.1f)
                                }
                            ) {
                                Text(
                                    text = projeto.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (projeto.statusApi == "ATRASADO") {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        SuccessGreen
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = projeto.progresso / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ItemProjeto("Investimento", projeto.investimento)
                            ItemProjeto("Retorno", Formatadores.moeda(projeto.retornoValor))
                            ItemProjeto("Prazo", projeto.prazo)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun fatia(rotulo: String, quantidade: Int?, cor: Color) = FatiaGrafico(
    rotulo = rotulo,
    valor = (quantidade ?: 0).toDouble(),
    cor = cor,
    valorFormatado = (quantidade ?: 0).toString()
)

@Composable
private fun SecaoGrafico(titulo: String, conteudo: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            conteudo()
        }
    }
}

@Composable
private fun ItemProjeto(rotulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Text(text = valor, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
    }
}

@Composable
fun KPIDashboard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun OrientacoesScreen(
    viewModel: LiderancaViewModel,
    onNova: () -> Unit,
    onEditar: (String) -> Unit,
    onHistorico: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var excluindo by remember { mutableStateOf<Orientacao?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orientações estratégicas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNova, containerColor = PrimaryBlue) {
                Icon(Icons.Filled.Add, contentDescription = "Nova orientação", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            uiState.errorMessage?.let { erro ->
                MensagemDeErro(mensagem = erro, onTentarNovamente = viewModel::carregar)
            }

            if (uiState.isLoading && uiState.orientacoes.isEmpty()) {
                Carregando()
            } else if (uiState.orientacoes.isEmpty()) {
                ListaVazia("Nenhuma orientação cadastrada\nClique no + para criar uma")
            }

            uiState.orientacoes.forEach { orientacao ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = orientacao.titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (orientacao.vigente) {
                                Surface(shape = RoundedCornerShape(20.dp), color = SuccessGreen.copy(alpha = 0.15f)) {
                                    Text(
                                        text = "Vigente",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${orientacao.area} · ${orientacao.periodo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        orientacao.indicadores.forEach { indicador ->
                            Text(
                                text = "✓ $indicador",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            TextButton(onClick = { onEditar(orientacao.id) }) { Text("Editar") }
                            TextButton(onClick = { onHistorico(orientacao.id) }) { Text("Histórico") }
                            TextButton(onClick = { excluindo = orientacao }) { Text("Excluir") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    excluindo?.let { alvo ->
        AlertDialog(
            onDismissRequest = { excluindo = null },
            title = { Text("Excluir orientação") },
            text = { Text("\"${alvo.titulo}\" deixará de aparecer nas listas. O histórico é preservado.") },
            confirmButton = {
                TextButton(onClick = {
                    excluindo = null
                    viewModel.excluirOrientacao(alvo.id)
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { excluindo = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun FormularioOrientacaoScreen(
    viewModel: LiderancaViewModel,
    orientacaoId: String? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val orientacao = orientacaoId?.let { id -> uiState.orientacoes.firstOrNull { it.id == id } }

    var titulo by remember(orientacao) { mutableStateOf(orientacao?.titulo.orEmpty()) }
    var descricao by remember(orientacao) { mutableStateOf(orientacao?.descricao.orEmpty()) }
    var categoria by remember(orientacao) { mutableStateOf(orientacao?.categoria.orEmpty()) }
    var campanha by remember(orientacao) { mutableStateOf(orientacao?.campanha.orEmpty()) }
    var area by remember(orientacao) { mutableStateOf(orientacao?.area.orEmpty()) }
    var periodo by remember(orientacao) { mutableStateOf(orientacao?.periodo.orEmpty()) }
    var indicadores by remember(orientacao) {
        mutableStateOf(orientacao?.indicadores.orEmpty().joinToString("\n"))
    }
    var vigente by remember(orientacao) { mutableStateOf(orientacao?.vigente ?: true) }

    LaunchedEffect(uiState.operacaoConcluida) {
        if (uiState.operacaoConcluida) {
            viewModel.operacaoTratada()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (orientacao == null) "Nova orientação" else "Editar orientação") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            uiState.errorMessage?.let { erro ->
                MensagemDeErro(mensagem = erro, onTentarNovamente = viewModel::carregar)
            }

            CampoTexto("Título", titulo) { titulo = it }
            CampoTexto("Descrição", descricao, linhas = 3) { descricao = it }
            CampoTexto("Categoria", categoria) { categoria = it }
            CampoTexto("Campanha", campanha) { campanha = it }
            CampoTexto("Área", area) { area = it }
            CampoTexto("Período", periodo) { periodo = it }
            CampoTexto("Indicadores (um por linha)", indicadores, linhas = 4) { indicadores = it }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = vigente, onCheckedChange = { vigente = it })
                Text(
                    text = "Orientação vigente",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            val podeSalvar = listOf(titulo, descricao, categoria, campanha, area, periodo).all { it.isNotBlank() }

            Button(
                onClick = {
                    viewModel.salvarOrientacao(
                        id = orientacao?.id,
                        titulo = titulo,
                        descricao = descricao,
                        categoria = categoria,
                        campanha = campanha,
                        area = area,
                        periodo = periodo,
                        indicadores = indicadores.lines().map { it.trim() }.filter { it.isNotBlank() },
                        vigente = vigente
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = podeSalvar && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (orientacao == null) "Criar orientação" else "Salvar alterações")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CampoTexto(rotulo: String, valor: String, linhas: Int = 1, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(rotulo) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        maxLines = linhas,
        singleLine = linhas == 1
    )
}

@Composable
fun HistoricoOrientacaoScreen(
    orientacaoId: String,
    viewModel: LiderancaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orientacaoId) {
        viewModel.carregarHistorico(orientacaoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            uiState.errorMessage?.let { erro ->
                MensagemDeErro(
                    mensagem = erro,
                    onTentarNovamente = { viewModel.carregarHistorico(orientacaoId) }
                )
            }

            if (uiState.isLoading && uiState.historico.isEmpty()) {
                Carregando()
            } else if (uiState.historico.isEmpty()) {
                ListaVazia("Sem registros de alteração")
            }

            uiState.historico.forEach { registro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (registro.acao) {
                                    "CRIACAO" -> "Criada"
                                    "ATUALIZACAO" -> "Atualizada"
                                    "EXCLUSAO" -> "Excluída"
                                    else -> registro.acao
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = registro.data,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = registro.titulo,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "${registro.categoria} · ${registro.campanha}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "por ${registro.autor}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetalheProjetoLiderancaScreen(
    projetoId: String,
    viewModel: LiderancaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val projeto = uiState.projetoSelecionado

    LaunchedEffect(projetoId) {
        viewModel.carregarProjeto(projetoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retorno do projeto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            uiState.errorMessage?.let { erro ->
                MensagemDeErro(
                    mensagem = erro,
                    onTentarNovamente = { viewModel.carregarProjeto(projetoId) }
                )
            }

            if (projeto == null) {
                Carregando()
                return@Column
            }

            Text(text = projeto.nome, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${projeto.status} · etapa ${projeto.etapa}/${projeto.totalEtapas} · prazo ${projeto.prazo}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            LinearProgressIndicator(
                progress = projeto.progresso / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard("INVESTIMENTO", projeto.investimento, Modifier.weight(1f))
                KPIDashboard("RETORNO", projeto.retorno, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard("LUCRO", projeto.lucro, Modifier.weight(1f))
                KPIDashboard("ROI", projeto.roi, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard("CUSTO EVITADO", projeto.custoEvitado, Modifier.weight(1f))
                KPIDashboard("PRODUTIVIDADE", projeto.produtividade, Modifier.weight(1f))
            }

            if (projeto.orientacaoTitulo.isNotBlank()) {
                Text(
                    text = "Vinculado a: ${projeto.orientacaoTitulo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
