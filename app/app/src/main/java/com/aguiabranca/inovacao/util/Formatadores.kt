package com.aguiabranca.inovacao.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/** Formatação dos dados da API para os textos exibidos nas telas. */
object Formatadores {

    private val PT_BR = Locale("pt", "BR")
    private val DIA_MES = DateTimeFormatter.ofPattern("dd MMM", PT_BR)

    /** 84000 -> "R$ 84k"; 1920000 -> "R$ 1,9M" */
    fun moeda(valor: Double?): String {
        if (valor == null) return "—"
        val absoluto = abs(valor)
        return when {
            absoluto >= 1_000_000 -> "R$ %s M".format(PT_BR, decimal(valor / 1_000_000)).replace(" M", "M")
            absoluto >= 1_000 -> "R$ ${(valor / 1_000).roundToLong()}k"
            else -> "R$ ${valor.roundToLong()}"
        }
    }

    /** 2.4 -> "2,4x" */
    fun multiplicador(valor: Double?): String = if (valor == null) "—" else "${decimal(valor)}x"

    /** 9.4 -> "+9,4%" */
    fun percentualComSinal(valor: Double?): String {
        if (valor == null) return "—"
        val sinal = if (valor >= 0) "+" else ""
        return "$sinal${decimal(valor)}%"
    }

    /** "2026-11-30" -> "30 nov" */
    fun dataCurta(iso: String?): String = runCatching {
        LocalDate.parse(iso).format(DIA_MES).lowercase(PT_BR)
    }.getOrDefault("—")

    /** Instante ISO -> "agora", "há 3 dias", "há 2 semanas" */
    fun tempoRelativo(iso: String?): String = runCatching {
        val dias = Duration.between(Instant.parse(iso), Instant.now()).toDays()
        when {
            dias <= 0L -> "hoje"
            dias == 1L -> "ontem"
            dias < 7L -> "há $dias dias"
            dias < 30L -> "há ${dias / 7} semana${if (dias / 7 > 1) "s" else ""}"
            else -> "há ${dias / 30} ${if (dias / 30 > 1) "meses" else "mês"}"
        }
    }.getOrDefault("")

    /** "NO_PRAZO" -> "No prazo" (as telas destacam o status pela palavra "prazo"). */
    fun statusProjeto(status: String?): String = when (status) {
        "NO_PRAZO" -> "No prazo"
        "ATRASADO" -> "Atrasado"
        "CONCLUIDO" -> "Concluído"
        "CANCELADO" -> "Cancelado"
        else -> status.orEmpty()
    }

    private fun decimal(valor: Double): String = String.format(PT_BR, "%.1f", valor)
}
