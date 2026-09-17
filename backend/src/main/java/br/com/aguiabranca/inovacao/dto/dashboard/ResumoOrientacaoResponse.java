package br.com.aguiabranca.inovacao.dto.dashboard;

/** Resultado consolidado por orientação estratégica — série pronta para gráfico de barras. */
public record ResumoOrientacaoResponse(
        String orientacaoId,
        String titulo,
        String campanha,
        String area,
        long ideias,
        IndicadoresResponse indicadores
) {
}
