package br.com.aguiabranca.inovacao.dto.projeto;

import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProgressoRequest(
        @NotNull(message = "Informe a etapa atual")
        @Min(value = 1, message = "A etapa começa em 1")
        Integer etapa,

        @NotNull(message = "Informe o progresso")
        @Min(value = 0, message = "Progresso mínimo é 0")
        @Max(value = 100, message = "Progresso máximo é 100")
        Integer progresso,

        /** Opcional: mantém o status atual quando ausente. */
        StatusProjeto status,

        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String observacao
) {
}
