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
import com.aguiabranca.inovacao.ui.components.Carregando
import com.aguiabranca.inovacao.ui.components.ListaVazia
import com.aguiabranca.inovacao.ui.components.MensagemDeErro
import com.aguiabranca.inovacao.ui.viewmodel.OperadorViewModel

private val PrimaryBlue = Color(0xFF4C63DD)
private val StatusAnalise = Color(0xFFFFA500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperadorHomeScreen(
    viewModel: OperadorViewModel = viewModel(),
    onNavigateToNewIdeia: () -> Unit,
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

            // Orientação em destaque
            if (uiState.ultimaOrientacao != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
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
                            text = uiState.ultimaOrientacao!!.titulo,
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
                IdeaCardOperador(idea)
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
fun IdeaCardOperador(idea: Idea) {
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
                    text = idea.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (idea.status) {
                        IdeaStatus.ANALISE -> StatusAnalise.copy(alpha = 0.2f)
                        else -> PrimaryBlue.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (idea.status) {
                            IdeaStatus.ANALISE -> "Em análise"
                            IdeaStatus.ENVIADA -> "Enviada"
                            IdeaStatus.PROJETO -> "Aprovada"
                            else -> idea.status.name
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (idea.status) {
                            IdeaStatus.ANALISE -> StatusAnalise
                            else -> PrimaryBlue
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaIdeiaScreen(
    viewModel: OperadorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val (titulo, setTitulo) = remember { mutableStateOf("") }
    val (categoria, setCategoria) = remember { mutableStateOf("") }
    val (problema, setProblema) = remember { mutableStateOf("") }
    val (proposta, setProposta) = remember { mutableStateOf("") }
    val (isLoading, setIsLoading) = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova ideia") },
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
            LinearProgressIndicator(
                progress = 0.33f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Passo 1 de 3",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryBlue,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = setTitulo,
                label = { Text("Título") },
                placeholder = { Text("Resuma sua ideia em 1 linha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                maxLines = 1
            )

            OutlinedTextField(
                value = categoria,
                onValueChange = setCategoria,
                label = { Text("Categoria") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                trailingIcon = {
                    Icon(Icons.Filled.ExpandMore, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = problema,
                onValueChange = setProblema,
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
                onValueChange = setProposta,
                label = { Text("Sua proposta") },
                placeholder = { Text("Como podemos resolver?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .heightIn(min = 100.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    setIsLoading(true)
                    viewModel.adicionarIdeia(titulo, categoria, problema, proposta)
                    setIsLoading(false)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading && titulo.isNotEmpty() && categoria.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Enviar")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
