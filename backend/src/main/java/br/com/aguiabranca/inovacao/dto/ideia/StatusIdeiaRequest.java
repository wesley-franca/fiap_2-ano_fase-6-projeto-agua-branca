package br.com.aguiabranca.inovacao.dto.ideia;

import br.com.aguiabranca.inovacao.domain.StatusIdeia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusIdeiaRequest(
        @NotNull(message = "Informe o novo status")
        StatusIdeia status,

        /** Obrigatório ao rejeitar, para o operador saber o motivo. */
        @Size(max = 500, message = "Comentário deve ter no máximo 500 caracteres")
        String comentario
) {
}
