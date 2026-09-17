package br.com.aguiabranca.inovacao.dto.projeto;

import br.com.aguiabranca.inovacao.domain.Projeto;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record ProjetoResponse(
        String id,
        String nome,
        String descricao,
        String responsavelId,
        String responsavelNome,
        String orientacaoId,
        String ideiaOrigemId,
        int etapa,
        int totalEtapas,
        int progresso,
        StatusProjeto status,
        LocalDate dataInicio,
        LocalDate prazo,
        BigDecimal investimento,
        BigDecimal retornoFinanceiro,
        BigDecimal custoEvitado,
        BigDecimal aumentoProdutividade,
        List<AtualizacaoResponse> atualizacoes,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static ProjetoResponse from(Projeto projeto) {
        return new ProjetoResponse(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDescricao(),
                projeto.getResponsavelId(),
                projeto.getResponsavelNome(),
                projeto.getOrientacaoId(),
                projeto.getIdeiaOrigemId(),
                projeto.getEtapa(),
                projeto.getTotalEtapas(),
                projeto.getProgresso(),
                projeto.getStatus(),
                projeto.getDataInicio(),
                projeto.getPrazo(),
                projeto.getInvestimento(),
                projeto.getRetornoFinanceiro(),
                projeto.getCustoEvitado(),
                projeto.getAumentoProdutividade(),
                projeto.getAtualizacoes().stream()
                        .sorted(Comparator.comparing(a -> a.data(), Comparator.reverseOrder()))
                        .map(AtualizacaoResponse::from)
                        .toList(),
                projeto.getCriadoEm(),
                projeto.getAtualizadoEm());
    }
}
