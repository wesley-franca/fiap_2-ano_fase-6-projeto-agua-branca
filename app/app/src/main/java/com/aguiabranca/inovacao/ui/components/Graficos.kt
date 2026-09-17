package com.aguiabranca.inovacao.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

/** Uma fatia de um gráfico: rótulo, valor e a cor usada no desenho. */
data class FatiaGrafico(val rotulo: String, val valor: Double, val cor: Color, val valorFormatado: String)

/**
 * Barras horizontais proporcionais ao maior valor da série.
 * Desenhadas em Canvas para não depender de biblioteca externa de gráficos.
 */
@Composable
fun GraficoBarras(fatias: List<FatiaGrafico>, modifier: Modifier = Modifier) {
    if (fatias.isEmpty()) {
        ListaVazia("Sem dados para exibir")
        return
    }
    val maior = max(fatias.maxOf { it.valor }, 1.0)

    Column(modifier = modifier.fillMaxWidth()) {
        fatias.forEach { fatia ->
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fatia.rotulo,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = fatia.valorFormatado, style = MaterialTheme.typography.labelSmall)
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .padding(top = 4.dp)
                ) {
                    val largura = size.width * (fatia.valor / maior).toFloat().coerceIn(0f, 1f)
                    drawRoundRect(
                        color = fatia.cor.copy(alpha = 0.15f),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                    )
                    if (largura > 0f) {
                        drawRoundRect(
                            color = fatia.cor,
                            size = size.copy(width = largura),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                        )
                    }
                }
            }
        }
    }
}

/** Barra única dividida por categoria (ex.: projetos no prazo x atrasados), com legenda. */
@Composable
fun GraficoDistribuicao(fatias: List<FatiaGrafico>, modifier: Modifier = Modifier) {
    val total = fatias.sumOf { it.valor }
    if (total <= 0.0) {
        ListaVazia("Sem dados para exibir")
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            var deslocamento = 0f
            fatias.forEach { fatia ->
                val largura = size.width * (fatia.valor / total).toFloat()
                if (largura > 0f) {
                    drawRect(
                        color = fatia.cor,
                        topLeft = androidx.compose.ui.geometry.Offset(deslocamento, 0f),
                        size = size.copy(width = largura)
                    )
                    deslocamento += largura
                }
            }
        }

        Column(modifier = Modifier.padding(top = 12.dp)) {
            fatias.filter { it.valor > 0 }.forEach { fatia ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = fatia.cor,
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = fatia.rotulo,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    Text(text = fatia.valorFormatado, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
