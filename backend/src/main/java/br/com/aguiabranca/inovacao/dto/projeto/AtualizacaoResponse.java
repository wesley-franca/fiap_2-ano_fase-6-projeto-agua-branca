package br.com.aguiabranca.inovacao.dto.projeto;

import br.com.aguiabranca.inovacao.domain.AtualizacaoProjeto;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;

import java.time.Instant;

public record AtualizacaoResponse(
        Instant data,
        Integer etapa,
        Integer progresso,
        StatusProjeto status,
        String observacao,
        String autorNome
) {

    public static AtualizacaoResponse from(AtualizacaoProjeto atualizacao) {
        return new AtualizacaoResponse(
                atualizacao.data(),
                atualizacao.etapa(),
                atualizacao.progresso(),
                atualizacao.status(),
                atualizacao.observacao(),
                atualizacao.autorNome());
    }
}
