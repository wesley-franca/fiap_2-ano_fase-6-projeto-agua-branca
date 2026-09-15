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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aguiabranca.inovacao.ui.viewmodel.LiderancaViewModel

private val SuccessGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiderancaDashboardScreen(
    viewModel: LiderancaViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liderança - Dashboard") },
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
                text = "Visão geral",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Q2 · 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // KPI Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard(
                    label = "ROI\nCONSOLIDADO",
                    value = uiState.metricas.roi,
                    change = "+0,3",
                    modifier = Modifier.weight(1f)
                )
                KPIDashboard(
                    label = "LUCRO YTD",
                    value = uiState.metricas.lucro,
                    change = "+18%",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard(
                    label = "PROJETOS\nATIVOS",
                    value = uiState.metricas.projetosAtivos.toString(),
                    change = "",
                    modifier = Modifier.weight(1f)
                )
                KPIDashboard(
                    label = "NO PRAZO",
                    value = "${uiState.metricas.noPrazo}/${uiState.metricas.projetosAtivos}",
                    change = "-4%",
                    modifier = Modifier.weight(1f)
                )
            }

            // Outras métricas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPIDashboard(
                    label = "CUSTO\nEVITADO",
                    value = uiState.metricas.custoEvitado,
                    change = "+12%",
                    modifier = Modifier.weight(1f)
                )
                KPIDashboard(
                    label = "PRODUTIVIDADE",
                    value = uiState.metricas.produtividade,
                    change = "+1,1pp",
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = "Orientações estratégicas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.orientacoes.forEach { orientacao ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = orientacao.titulo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${orientacao.area} · ${orientacao.periodo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✓ ${orientacao.indicador1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = "Andamento de projetos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            uiState.projetos.forEach { projeto ->
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
                                color = when {
                                    projeto.status.contains("prazo") -> SuccessGreen.copy(alpha = 0.1f)
                                    else -> Color.Red.copy(alpha = 0.1f)
                                }
                            ) {
                                Text(
                                    text = projeto.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        projeto.status.contains("prazo") -> SuccessGreen
                                        else -> Color.Red
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
                            Text(
                                text = "Investimento:\n${projeto.investimento}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Prazo:\n${projeto.prazo}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Progresso:\n${projeto.progresso}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
fun KPIDashboard(
    label: String,
    value: String,
    change: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
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
            if (change.isNotEmpty()) {
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
