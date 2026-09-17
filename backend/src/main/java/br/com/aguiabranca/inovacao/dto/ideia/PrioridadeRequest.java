package br.com.aguiabranca.inovacao.dto.ideia;

import br.com.aguiabranca.inovacao.domain.PrioridadeIdeia;
import jakarta.validation.constraints.NotNull;

public record PrioridadeRequest(
        @NotNull(message = "Informe a prioridade")
        PrioridadeIdeia prioridade
) {
}
