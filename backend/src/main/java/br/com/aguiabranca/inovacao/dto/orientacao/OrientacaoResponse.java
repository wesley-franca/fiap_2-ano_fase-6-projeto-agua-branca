package br.com.aguiabranca.inovacao.dto.orientacao;

import br.com.aguiabranca.inovacao.domain.Orientacao;

import java.time.Instant;
import java.util.List;

public record OrientacaoResponse(
        String id,
        String titulo,
        String descricao,
        String categoria,
        String campanha,
        String area,
        String periodo,
        List<String> indicadores,
        boolean vigente,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static OrientacaoResponse from(Orientacao orientacao) {
        return new OrientacaoResponse(
                orientacao.getId(),
                orientacao.getTitulo(),
                orientacao.getDescricao(),
                orientacao.getCategoria(),
                orientacao.getCampanha(),
                orientacao.getArea(),
                orientacao.getPeriodo(),
                orientacao.getIndicadores(),
                orientacao.isVigente(),
                orientacao.getCriadoEm(),
                orientacao.getAtualizadoEm());
    }
}
