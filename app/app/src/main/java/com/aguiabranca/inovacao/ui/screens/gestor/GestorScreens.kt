@file:OptIn(ExperimentalMaterial3Api::class)

package com.aguiabranca.inovacao.ui.screens.gestor

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aguiabranca.inovacao.ui.viewmodel.GestorViewModel

private val PrimaryBlue = Color(0xFF4C63DD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestorPainelScreen(
    viewModel: GestorViewModel = viewModel(),
    onNavigateToFilaIdeias: () -> Unit,
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
            Text(
                text = "Painel do gestor",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${uiState.kpisNovas} ideias aguardando você",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // KPI Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIBox(
                    title = "IDEIAS\nNOVAS",
                    value = uiState.kpisNovas.toString(),
                    modifier = Modifier.weight(1f)
                )
                KPIBox(
                    title = "EM\nANÁLISE",
                    value = uiState.kpisEmAnalise.toString(),
                    modifier = Modifier.weight(1f)
                )
                KPIBox(
                    title = "PROJETOS\nATIVOS",
                    value = uiState.kpisAtivos.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Ideias para priorizar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.ideiasParaAprovar.take(2).forEach { idea ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = idea.titulo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
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
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PrimaryBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = idea.prioridade.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = idea.categoria,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            TextButton(onClick = onNavigateToFilaIdeias) {
                Text("ver fila ›", color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Meus projetos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.meusProjetos.take(2).forEach { projeto ->
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
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = projeto.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50),
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Prazo: ${projeto.prazo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = projeto.investimento,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun KPIBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.background(
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilaIdeiasScreen(
    viewModel: GestorViewModel = viewModel(),
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
            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Todas · ${uiState.ideiasParaAprovar.size}") }
                )
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Por área") }
                )
            }

            uiState.ideiasParaAprovar.forEach { idea ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = idea.titulo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${idea.nomeOperador} · ${idea.criadoEm}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PrimaryBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = idea.prioridade.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = idea.categoria,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.aprovarIdeia(idea.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Aprovar", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.rejeitarIdeia(idea.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB6C1)
                                )
                            ) {
                                Text("Rejeitar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
