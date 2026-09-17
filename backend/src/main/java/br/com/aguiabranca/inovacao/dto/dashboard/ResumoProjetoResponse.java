package br.com.aguiabranca.inovacao.dto.dashboard;

import br.com.aguiabranca.inovacao.domain.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoProjetoResponse(
        String id,
        String nome,
        StatusProjeto status,
        int etapa,
        int totalEtapas,
        int progresso,
        LocalDate prazo,
        String orientacaoId,
        String orientacaoTitulo,
        BigDecimal investimento,
        BigDecimal retornoFinanceiro,
        BigDecimal lucro,
        BigDecimal roi,
        BigDecimal custoEvitado,
        BigDecimal aumentoProdutividade
) {
}
