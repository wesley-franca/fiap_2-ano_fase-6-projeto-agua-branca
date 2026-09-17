package br.com.aguiabranca.inovacao.dto.ideia;

import br.com.aguiabranca.inovacao.domain.Ideia;
import br.com.aguiabranca.inovacao.domain.Impacto;
import br.com.aguiabranca.inovacao.domain.PrioridadeIdeia;
import br.com.aguiabranca.inovacao.domain.StatusIdeia;

import java.time.Instant;

public record IdeiaResponse(
        String id,
        String titulo,
        String categoria,
        String problemaObservado,
        String suaProposta,
        Impacto impacto,
        StatusIdeia status,
        PrioridadeIdeia prioridade,
        String operadorId,
        String nomeOperador,
        String area,
        String orientacaoId,
        String comentarioAvaliacao,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static IdeiaResponse from(Ideia ideia) {
        return new IdeiaResponse(
                ideia.getId(),
                ideia.getTitulo(),
                ideia.getCategoria(),
                ideia.getProblemaObservado(),
                ideia.getSuaProposta(),
                ideia.getImpacto(),
                ideia.getStatus(),
                ideia.getPrioridade(),
                ideia.getOperadorId(),
                ideia.getNomeOperador(),
                ideia.getArea(),
                ideia.getOrientacaoId(),
                ideia.getComentarioAvaliacao(),
                ideia.getCriadoEm(),
                ideia.getAtualizadoEm());
    }
}
