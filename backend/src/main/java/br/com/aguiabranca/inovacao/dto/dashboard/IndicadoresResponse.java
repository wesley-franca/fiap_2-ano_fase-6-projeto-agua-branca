package br.com.aguiabranca.inovacao.dto.dashboard;

import java.math.BigDecimal;

/** Indicadores financeiros consolidados de um conjunto de projetos. */
public record IndicadoresResponse(
        long totalProjetos,
        long projetosAtivos,
        long projetosNoPrazo,
        long projetosConcluidos,
        BigDecimal percentualNoPrazo,
        BigDecimal investimentoTotal,
        BigDecimal retornoTotal,
        BigDecimal lucro,
        BigDecimal roi,
        BigDecimal custoEvitadoTotal,
        BigDecimal produtividadeMedia
) {
}
