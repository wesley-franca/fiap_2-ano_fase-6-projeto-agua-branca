package br.com.aguiabranca.inovacao.domain;

import java.time.Instant;

public record AtualizacaoProjeto(
        Instant data,
        Integer etapa,
        Integer progresso,
        StatusProjeto status,
        String observacao,
        String autorId,
        String autorNome
) {
}
