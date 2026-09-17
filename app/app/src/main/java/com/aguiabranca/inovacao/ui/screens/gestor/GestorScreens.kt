@file:OptIn(ExperimentalMaterial3Api::class)

package com.aguiabranca.inovacao.ui.screens.gestor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aguiabranca.inovacao.data.model.Idea
import com.aguiabranca.inovacao.data.model.IdeaPriority
import com.aguiabranca.inovacao.data.model.Orientacao
import com.aguiabranca.inovacao.data.model.Projeto
import com.aguiabranca.inovacao.ui.components.Carregando
import com.aguiabranca.inovacao.ui.components.ListaVazia
import com.aguiabranca.inovacao.ui.components.MensagemDeErro
import com.aguiabranca.inovacao.ui.viewmodel.FiltroIdeia
import com.aguiabranca.inovacao.ui.viewmodel.GestorViewModel

private val PrimaryBlue = Color(0xFF4C63DD)
private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun GestorPainelScreen(
    viewModel: GestorViewModel,
    onNavigateToFilaIdeias: () -> Unit,
    onNavigateToProjetos: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor") },
                actions = {
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
            Text(text = "Painel do gestor", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "${uiState.kpisNovas} ideias aguardando você",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIBox("IDEIAS\nNOVAS", uiState.kpisNovas.toString(), Modifier.weight(1f))
                KPIBox("EM\nANÁLISE", uiState.kpisEmAnalise.toString(), Modifier.weight(1f))
                KPIBox("PROJETOS\nATIVOS", uiState.kpisAtivos.toString(), Modifier.weight(1f))
            }

            uiState.errorMessage?.let { erro ->
                MensagemDeErro(mensagem = erro, onTentarNovamente = viewModel::carregar)
            }

            Text(
                text = "Ideias para priorizar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (uiState.isLoading && uiState.ideiasParaAprovar.isEmpty()) {
                Carregando()
            } else if (uiState.ideiasParaAprovar.isEmpty()) {
                ListaVazia("Nenhuma ideia aguardando avaliação")
            }

            uiState.ideiasParaAprovar.take(2).forEach { idea ->
                CardIdeiaResumo(idea)
            }

            TextButton(onClick = onNavigateToFilaIdeias) {
                Text("ver fila ›", color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Meus projetos", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNavigateToProjetos) {
                    Text("gerenciar ›", color = PrimaryBlue)
                }
            }

            if (uiState.meusProjetos.isEmpty() && !uiState.isLoading) {
                ListaVazia("Nenhum projeto cadastrado")
            }

            uiState.meusProjetos.take(2).forEach { projeto ->
                CardProjeto(projeto)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CardIdeiaResumo(idea: Idea, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = idea.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${idea.nomeOperador} · ${idea.criadoEm}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Etiqueta(idea.prioridade.name)
                Text(
                    text = idea.categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, cor: Color = PrimaryBlue) {
    Surface(shape = RoundedCornerShape(20.dp), color = cor.copy(alpha = 0.1f)) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = cor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CardProjeto(projeto: Projeto, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = projeto.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Etapa ${projeto.etapa}/${projeto.totalEtapas}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Etiqueta(
                    texto = projeto.status,
                    cor = if (projeto.statusApi == "ATRASADO") MaterialTheme.colorScheme.error else SuccessGreen
                )
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prazo: ${projeto.prazo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(text = projeto.investimento, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun KPIBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun FilaIdeiasScreen(
    viewModel: GestorViewModel,
    onAbrirIdeia: (String) -> Unit,
    onBackPress: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fila de ideias") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroIdeia.entries.forEach { filtro ->
                    val rotulo = if (filtro == FiltroIdeia.TODAS) {
                        "${filtro.rotulo} · ${uiState.ideiasParaAprovar.size}"
                    } else {
                        filtro.rotulo
                    }
                    FilterChip(
                        selected = uiState.filtro == filtro,
                        onClick = { viewModel.aplicarFiltro(filtro) },
                        label = { Text(rotulo) }
                    )
                }
            }

            if (uiState.areas.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.areaSelecionada == null,
                        onClick = { viewModel.filtrarPorArea(null) },
                        label = { Text("Todas as áreas") }
                    )
                    uiState.areas.forEach { area ->
                        FilterChip(
                            selected = uiState.areaSelecionada == area,
                            onClick = { viewModel.filtrarPorArea(area) },
                            label = { Text(area) }
                        )
                    }
                }
            }

            uiState.errorMessage?.let { erro ->
                MensagemDeErro(mensagem = erro, onTentarNovamente = viewModel::carregar)
            }

            val ideias = uiState.ideiasFiltradas
            if (uiState.isLoading && ideias.isEmpty()) {
                Carregando()
            } else if (ideias.isEmpty()) {
                ListaVazia("Nenhuma ideia neste filtro")
            }

            ideias.forEach { idea ->
                CardIdeiaResumo(idea, onClick = { onAbrirIdeia(idea.id) })
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetalheIdeiaGestorScreen(
    ideiaId: String,
    viewModel: GestorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val ideia = uiState.ideiasParaAprovar.firstOrNull { it.id == ideiaId }
    var rejeitando by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }

    LaunchedEffect(ideia == null) {
        if (ideia == null && !uiState.isLoading) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analisar ideia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (ideia == null) {
            Carregando(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

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

            Text(text = ideia.titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${ideia.nomeOperador} · ${ideia.area} · ${ideia.criadoEm}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Campo("Problema observado", ideia.problemaObservado)
            Campo("Proposta", ideia.suaProposta)
            Campo("Status atual", ideia.status.name)

            Text(
                text = "Prioridade",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryBlue,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IdeaPriority.entries.forEach { prioridade ->
                    FilterChip(
                        selected = ideia.prioridade == prioridade,
                        onClick = { viewModel.priorizarIdeia(ideia.id, prioridade) },
                        label = { Text(prioridade.name) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.aprovarIdeia(ideia.id) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Avançar etapa")
                }
                OutlinedButton(
                    onClick = { rejeitando = true },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Rejeitar")
                }
            }

            Text(
                text = "Avançar leva a ideia para a próxima etapa do fluxo até virar projeto.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (rejeitando) {
        AlertDialog(
            onDismissRequest = { rejeitando = false },
            title = { Text("Rejeitar ideia") },
            text = {
                Column {
                    Text("Explique o motivo para o operador:")
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = motivo.isNotBlank(),
                    onClick = {
                        rejeitando = false
                        viewModel.rejeitarIdeia(ideiaId, motivo.trim())
                        motivo = ""
                    }
                ) { Text("Rejeitar") }
            },
            dismissButton = {
                TextButton(onClick = { rejeitando = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun Campo(rotulo: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = rotulo, style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
        Text(
            text = valor.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ProjetosGestorScreen(
    viewModel: GestorViewModel,
    onNovoProjeto: () -> Unit,
    onAbrirProjeto: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projetos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNovoProjeto, containerColor = PrimaryBlue) {
                Icon(Icons.Filled.Add, contentDescription = "Novo projeto", tint = Color.White)
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

            if (uiState.isLoading && uiState.meusProjetos.isEmpty()) {
                Carregando()
            } else if (uiState.meusProjetos.isEmpty()) {
                ListaVazia("Nenhum projeto cadastrado\nClique no + para criar um")
            }

            uiState.meusProjetos.forEach { projeto ->
                CardProjeto(projeto, onClick = { onAbrirProjeto(projeto.id) })
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DetalheProjetoScreen(
    projetoId: String,
    viewModel: GestorViewModel,
    onEditar: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val projeto = uiState.meusProjetos.firstOrNull { it.id == projetoId }
    var confirmandoExclusao by remember { mutableStateOf(false) }
    var etapa by remember(projeto) { mutableStateOf(projeto?.etapa?.toString().orEmpty()) }
    var progresso by remember(projeto) { mutableStateOf(projeto?.progresso?.toString().orEmpty()) }
    var statusSelecionado by remember(projeto) { mutableStateOf(projeto?.statusApi ?: "NO_PRAZO") }
    var observacao by remember(projeto) { mutableStateOf("") }
    var retorno by remember(projeto) { mutableStateOf(projeto?.retornoValor?.toString().orEmpty()) }
    var custoEvitado by remember(projeto) { mutableStateOf(projeto?.custoEvitadoValor?.toString().orEmpty()) }
    var produtividade by remember(projeto) { mutableStateOf(projeto?.produtividadeValor?.toString().orEmpty()) }

    LaunchedEffect(projeto == null) {
        if (projeto == null && !uiState.isLoading) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projeto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditar(projetoId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { confirmandoExclusao = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (projeto == null) {
            Carregando(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

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

            Text(text = projeto.nome, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${projeto.status} · prazo ${projeto.prazo} · ${projeto.investimento}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            if (projeto.descricao.isNotBlank()) {
                Campo("Descrição", projeto.descricao)
            }

            Text(text = "Acompanhamento", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = etapa,
                    onValueChange = { etapa = it.filter(Char::isDigit) },
                    label = { Text("Etapa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = progresso,
                    onValueChange = { progresso = it.filter(Char::isDigit) },
                    label = { Text("Progresso %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("NO_PRAZO", "ATRASADO", "CONCLUIDO", "CANCELADO").forEach { status ->
                    FilterChip(
                        selected = statusSelecionado == status,
                        onClick = { statusSelecionado = status },
                        label = { Text(status.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)) }
                    )
                }
            }

            OutlinedTextField(
                value = observacao,
                onValueChange = { observacao = it },
                label = { Text("Observação desta atualização") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                maxLines = 3
            )

            Button(
                onClick = {
                    viewModel.registrarProgresso(
                        id = projeto.id,
                        etapa = etapa.toIntOrNull() ?: projeto.etapa,
                        progresso = progresso.toIntOrNull() ?: projeto.progresso,
                        status = statusSelecionado,
                        observacao = observacao.ifBlank { null }
                    )
                    observacao = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Salvar progresso")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(text = "Resultados obtidos", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = retorno,
                onValueChange = { retorno = it },
                label = { Text("Retorno financeiro (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = custoEvitado,
                onValueChange = { custoEvitado = it },
                label = { Text("Custo evitado (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = produtividade,
                onValueChange = { produtividade = it },
                label = { Text("Aumento de produtividade (pp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    viewModel.registrarResultados(
                        id = projeto.id,
                        retorno = retorno.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        custoEvitado = custoEvitado.replace(',', '.').toDoubleOrNull(),
                        produtividade = produtividade.replace(',', '.').toDoubleOrNull()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                enabled = !uiState.isLoading && retorno.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Salvar resultados")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (confirmandoExclusao) {
        AlertDialog(
            onDismissRequest = { confirmandoExclusao = false },
            title = { Text("Excluir projeto") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoExclusao = false
                    viewModel.excluirProjeto(projetoId)
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun FormularioProjetoScreen(
    viewModel: GestorViewModel,
    projetoId: String? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val projeto = projetoId?.let { id -> uiState.meusProjetos.firstOrNull { it.id == id } }

    var nome by remember(projeto) { mutableStateOf(projeto?.nome.orEmpty()) }
    var descricao by remember(projeto) { mutableStateOf(projeto?.descricao.orEmpty()) }
    var dataInicio by remember(projeto) { mutableStateOf(projeto?.dataInicioIso.orEmpty()) }
    var prazo by remember(projeto) { mutableStateOf(projeto?.prazoIso.orEmpty()) }
    var investimento by remember(projeto) {
        mutableStateOf(projeto?.investimentoValor?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var totalEtapas by remember(projeto) { mutableStateOf((projeto?.totalEtapas ?: 4).toString()) }
    var orientacao by remember(projeto, uiState.orientacoes) {
        mutableStateOf(uiState.orientacoes.firstOrNull { it.id == projeto?.orientacaoId })
    }

    LaunchedEffect(uiState.operacaoConcluida) {
        if (uiState.operacaoConcluida) {
            viewModel.operacaoTratada()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projeto == null) "Novo projeto" else "Editar projeto") },
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

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do projeto") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                maxLines = 3
            )

            SeletorOrientacao(
                orientacoes = uiState.orientacoes,
                selecionada = orientacao,
                onSelecionar = { orientacao = it }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dataInicio,
                    onValueChange = { dataInicio = it },
                    label = { Text("Início") },
                    placeholder = { Text("2026-09-01") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = prazo,
                    onValueChange = { prazo = it },
                    label = { Text("Prazo") },
                    placeholder = { Text("2027-03-31") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = investimento,
                    onValueChange = { investimento = it },
                    label = { Text("Investimento (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = totalEtapas,
                    onValueChange = { totalEtapas = it.filter(Char::isDigit) },
                    label = { Text("Etapas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            val podeSalvar = nome.isNotBlank() && orientacao != null &&
                    dataInicio.isNotBlank() && prazo.isNotBlank() && investimento.isNotBlank()

            Button(
                onClick = {
                    viewModel.salvarProjeto(
                        id = projeto?.id,
                        nome = nome,
                        descricao = descricao,
                        orientacaoId = orientacao?.id.orEmpty(),
                        ideiaOrigemId = projeto?.ideiaOrigemId,
                        totalEtapas = totalEtapas.toIntOrNull() ?: 4,
                        dataInicio = dataInicio.trim(),
                        prazo = prazo.trim(),
                        investimento = investimento.replace(',', '.').toDoubleOrNull() ?: 0.0
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = podeSalvar && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (projeto == null) "Criar projeto" else "Salvar alterações")
            }

            Text(
                text = "Datas no formato ano-mês-dia, por exemplo 2026-12-31.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SeletorOrientacao(
    orientacoes: List<Orientacao>,
    selecionada: Orientacao?,
    onSelecionar: (Orientacao) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = aberto,
        onExpandedChange = { aberto = !aberto },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = selecionada?.titulo ?: "Selecione uma orientação",
            onValueChange = {},
            readOnly = true,
            label = { Text("Orientação estratégica") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            orientacoes.forEach { orientacao ->
                DropdownMenuItem(
                    text = { Text(orientacao.titulo) },
                    onClick = {
                        onSelecionar(orientacao)
                        aberto = false
                    }
                )
            }
        }
    }
}
