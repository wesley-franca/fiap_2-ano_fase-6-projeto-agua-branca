package br.com.aguiabranca.inovacao.dto.orientacao;

import br.com.aguiabranca.inovacao.domain.AcaoHistorico;
import br.com.aguiabranca.inovacao.domain.HistoricoOrientacao;

import java.time.Instant;

public record HistoricoResponse(
        Instant data,
        AcaoHistorico acao,
        String titulo,
        String categoria,
        String campanha,
        String alteradoPorNome
) {

    public static HistoricoResponse from(HistoricoOrientacao historico) {
        return new HistoricoResponse(
                historico.data(),
                historico.acao(),
                historico.titulo(),
                historico.categoria(),
                historico.campanha(),
                historico.alteradoPorNome());
    }
}
