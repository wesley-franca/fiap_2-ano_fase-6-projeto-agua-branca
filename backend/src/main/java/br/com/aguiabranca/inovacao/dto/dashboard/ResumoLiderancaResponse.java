package br.com.aguiabranca.inovacao.dto.dashboard;

import java.util.List;
import java.util.Map;

/** Visão executiva: indicadores gerais, séries para gráficos e recorte por orientação. */
public record ResumoLiderancaResponse(
        IndicadoresResponse indicadores,
        Map<String, Long> projetosPorStatus,
        Map<String, Long> ideiasPorStatus,
        List<ResumoOrientacaoResponse> porOrientacao
) {
}
