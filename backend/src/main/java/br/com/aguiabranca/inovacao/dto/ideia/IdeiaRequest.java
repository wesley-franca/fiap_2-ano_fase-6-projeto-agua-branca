package br.com.aguiabranca.inovacao.dto.ideia;

import br.com.aguiabranca.inovacao.domain.Impacto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdeiaRequest(
        @NotBlank(message = "Informe o título")
        @Size(max = 120, message = "Título deve ter no máximo 120 caracteres")
        String titulo,

        @NotBlank(message = "Informe a categoria")
        String categoria,

        @NotBlank(message = "Descreva o problema observado")
        @Size(max = 1000, message = "Problema observado deve ter no máximo 1000 caracteres")
        String problemaObservado,

        @NotBlank(message = "Descreva sua proposta")
        @Size(max = 1000, message = "Proposta deve ter no máximo 1000 caracteres")
        String suaProposta,

        /** Opcional: ausente equivale a impacto médio. */
        Impacto impacto,

        @NotBlank(message = "Vincule a ideia a uma orientação estratégica")
        String orientacaoId
) {

    public Impacto impactoOuMedio() {
        return impacto == null ? Impacto.MEDIO : impacto;
    }
}
