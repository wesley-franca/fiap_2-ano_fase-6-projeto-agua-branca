package br.com.aguiabranca.inovacao.domain;

import java.time.Instant;

public record HistoricoOrientacao(
        Instant data,
        AcaoHistorico acao,
        String titulo,
        String categoria,
        String campanha,
        String alteradoPorId,
        String alteradoPorNome
) {
}
