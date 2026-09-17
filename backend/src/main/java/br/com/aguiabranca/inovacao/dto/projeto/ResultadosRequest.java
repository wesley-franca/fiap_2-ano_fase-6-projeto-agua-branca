package br.com.aguiabranca.inovacao.dto.projeto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ResultadosRequest(
        @NotNull(message = "Informe o retorno financeiro")
        @DecimalMin(value = "0.0", message = "Retorno financeiro não pode ser negativo")
        BigDecimal retornoFinanceiro,

        @DecimalMin(value = "0.0", message = "Custo evitado não pode ser negativo")
        BigDecimal custoEvitado,

        /** Aumento de produtividade em pontos percentuais (ex.: 9.4 = +9,4%). */
        BigDecimal aumentoProdutividade
) {
}
