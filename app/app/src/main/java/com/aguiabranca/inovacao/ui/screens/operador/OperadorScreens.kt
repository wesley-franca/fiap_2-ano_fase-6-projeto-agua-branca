@file:OptIn(ExperimentalMaterial3Api::class)

package com.aguiabranca.inovacao.ui.screens.operador

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aguiabranca.inovacao.data.model.Idea
import com.aguiabranca.inovacao.data.model.IdeaStatus
import com.aguiabranca.inovacao.data.model.Orientacao
import com.aguiabranca.inovacao.ui.components.Carregando
import com.aguiabranca.inovacao.ui.components.ListaVazia
import com.aguiabranca.inovacao.ui.components.MensagemDeErro
import com.aguiabranca.inovacao.ui.viewmodel.OperadorViewModel
import com.aguiabranca.inovacao.ui.viewmodel.podeSerEditada

private val PrimaryBlue = Color(0xFF4C63DD)
private val StatusAnalise = Color(0xFFFFA500)

@Composable
fun OperadorHomeScreen(
    viewModel: OperadorViewModel = viewModel(),
    onNavigateToNewIdeia: () -> Unit,
    onAbrirIdeia: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewIdeia,
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova ideia", tint = Color.White)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Operador") },
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
            Text(
                text = "Olá, Operador",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Bom dia · turno A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            uiState.ultimaOrientacao?.let { orientacao ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Orientações estratégicas",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Foco do trimestre",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = orientacao.titulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.errorMessage?.let { erro ->
                MensagemDeErro(mensagem = erro, onTentarNovamente = viewModel::carregar)
            }

            Text(
                text = "Minhas ideias",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.minhasIdeias.forEach { idea ->
                IdeaCardOperador(idea, onClick = { onAbrirIdeia(idea.id) })
            }

            if (uiState.isLoading && uiState.minhasIdeias.isEmpty()) {
                Carregando()
            } else if (uiState.minhasIdeias.isEmpty()) {
                ListaVazia("Nenhuma ideia cadastrada\nClique no + para criar uma")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun IdeaCardOperador(idea: Idea, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
                    text = idea.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaStatus(idea.status)
            }

            Text(
                text = idea.categoria,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = idea.criadoEm,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EtiquetaStatus(status: IdeaStatus) {
    val cor = when (status) {
        IdeaStatus.ANALISE, IdeaStatus.TRIAGEM, IdeaStatus.DECISAO -> StatusAnalise
        IdeaStatus.REJEITADA -> MaterialTheme.colorScheme.error
        else -> PrimaryBlue
    }
    Surface(shape = RoundedCornerShape(20.dp), color = cor.copy(alpha = 0.2f)) {
        Text(
            text = rotuloStatus(status),
            style = MaterialTheme.typography.labelSmall,
            color = cor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

private fun rotuloStatus(status: IdeaStatus) = when (status) {
    IdeaStatus.ENVIADA -> "Enviada"
    IdeaStatus.TRIAGEM -> "Em triagem"
    IdeaStatus.ANALISE -> "Em análise"
    IdeaStatus.DECISAO -> "Em decisão"
    IdeaStatus.PROJETO -> "Aprovada"
    IdeaStatus.REJEITADA -> "Rejeitada"
}

@Composable
fun DetalheIdeiaScreen(
    ideiaId: String,
    viewModel: OperadorViewModel,
    onEditar: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val ideia = uiState.minhasIdeias.firstOrNull { it.id == ideiaId }
    var confirmandoExclusao by remember { mutableStateOf(false) }

    // Depois de excluir, a ideia some da lista e a tela volta sozinha.
    LaunchedEffect(ideia == null) {
        if (ideia == null && !uiState.isLoading) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha ideia") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ideia.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaStatus(ideia.status)
            }

            Text(
                text = "${ideia.categoria} · ${ideia.criadoEm}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            CampoLeitura("Problema observado", ideia.problemaObservado)
            CampoLeitura("Sua proposta", ideia.suaProposta)
            CampoLeitura("Prioridade", ideia.prioridade.name)

            if (ideia.comentarioAvaliacao.isNotBlank()) {
                CampoLeitura("Retorno do gestor", ideia.comentarioAvaliacao)
            }

            if (ideia.podeSerEditada()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEditar(ideia.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Editar")
                    }
                    OutlinedButton(
                        onClick = { confirmandoExclusao = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Excluir")
                    }
                }
            } else {
                Text(
                    text = "Esta ideia já está em avaliação e não pode mais ser alterada.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (confirmandoExclusao) {
        AlertDialog(
            onDismissRequest = { confirmandoExclusao = false },
            title = { Text("Excluir ideia") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoExclusao = false
                    viewModel.excluirIdeia(ideiaId)
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CampoLeitura(rotulo: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryBlue
        )
        Text(
            text = valor.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Mesma tela para cadastrar e para editar: com `ideiaId`, os campos vêm preenchidos. */
@Composable
fun FormularioIdeiaScreen(
    viewModel: OperadorViewModel,
    ideiaId: String? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val ideia = ideiaId?.let { id -> uiState.minhasIdeias.firstOrNull { it.id == id } }

    var titulo by remember(ideia) { mutableStateOf(ideia?.titulo.orEmpty()) }
    var categoria by remember(ideia) { mutableStateOf(ideia?.categoria.orEmpty()) }
    var problema by remember(ideia) { mutableStateOf(ideia?.problemaObservado.orEmpty()) }
    var proposta by remember(ideia) { mutableStateOf(ideia?.suaProposta.orEmpty()) }
    var orientacaoSelecionada by remember(ideia, uiState.orientacoes) {
        mutableStateOf(
            uiState.orientacoes.firstOrNull { it.id == ideia?.orientacaoId }
                ?: uiState.ultimaOrientacao
        )
    }
    var enviando by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.operacaoConcluida) {
        if (uiState.operacaoConcluida) {
            viewModel.operacaoTratada()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ideia == null) "Nova ideia" else "Editar ideia") },
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
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                placeholder = { Text("Resuma sua ideia em 1 linha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                maxLines = 1
            )

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoria") },
                placeholder = { Text("Manutenção, Operação, Segurança...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                maxLines = 1
            )

            SeletorDeOrientacao(
                orientacoes = uiState.orientacoes,
                selecionada = orientacaoSelecionada,
                onSelecionar = { orientacaoSelecionada = it }
            )

            OutlinedTextField(
                value = problema,
                onValueChange = { problema = it },
                label = { Text("Problema observado") },
                placeholder = { Text("O que você vê acontecer no dia a dia?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .heightIn(min = 100.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = proposta,
                onValueChange = { proposta = it },
                label = { Text("Sua proposta") },
                placeholder = { Text("Como podemos resolver?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .heightIn(min = 100.dp),
                maxLines = 4
            )

            val podeEnviar = titulo.isNotBlank() && categoria.isNotBlank() &&
                    problema.isNotBlank() && proposta.isNotBlank() && orientacaoSelecionada != null

            Button(
                onClick = {
                    val orientacaoId = orientacaoSelecionada?.id ?: return@Button
                    enviando = true
                    if (ideia == null) {
                        viewModel.adicionarIdeia(titulo, categoria, problema, proposta, orientacaoId)
                    } else {
                        viewModel.atualizarIdeia(ideia.id, titulo, categoria, problema, proposta, orientacaoId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = podeEnviar && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (uiState.isLoading && enviando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (ideia == null) "Enviar" else "Salvar alterações")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SeletorDeOrientacao(
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
