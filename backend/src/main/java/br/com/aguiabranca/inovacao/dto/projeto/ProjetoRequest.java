package br.com.aguiabranca.inovacao.dto.projeto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoRequest(
        @NotBlank(message = "Informe o nome do projeto")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @NotBlank(message = "Vincule o projeto a uma orientação estratégica")
        String orientacaoId,

        /** Opcional: ideia aprovada que originou o projeto. */
        String ideiaOrigemId,

        @Min(value = 1, message = "O projeto deve ter ao menos 1 etapa")
        @Max(value = 20, message = "O projeto deve ter no máximo 20 etapas")
        Integer totalEtapas,

        @NotNull(message = "Informe a data de início")
        LocalDate dataInicio,

        @NotNull(message = "Informe o prazo")
        LocalDate prazo,

        @NotNull(message = "Informe o investimento")
        @DecimalMin(value = "0.0", message = "Investimento não pode ser negativo")
        BigDecimal investimento
) {

    public int totalEtapasOuPadrao() {
        return totalEtapas == null ? 4 : totalEtapas;
    }

    @AssertTrue(message = "O prazo não pode ser anterior à data de início")
    public boolean isPrazoCoerente() {
        return dataInicio == null || prazo == null || !prazo.isBefore(dataInicio);
    }
}
