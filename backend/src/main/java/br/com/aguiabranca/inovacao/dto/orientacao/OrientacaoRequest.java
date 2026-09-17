package br.com.aguiabranca.inovacao.dto.orientacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrientacaoRequest(
        @NotBlank(message = "Informe o título")
        @Size(max = 120, message = "Título deve ter no máximo 120 caracteres")
        String titulo,

        @NotBlank(message = "Informe a descrição")
        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @NotBlank(message = "Informe a categoria")
        String categoria,

        @NotBlank(message = "Informe a campanha")
        String campanha,

        @NotBlank(message = "Informe a área")
        String area,

        @NotBlank(message = "Informe o período")
        String periodo,

        @Size(max = 10, message = "Informe no máximo 10 indicadores")
        List<@NotBlank(message = "Indicador não pode ser vazio") String> indicadores,

        /** Opcional: ausente ou nulo equivale a não vigente. */
        Boolean vigente
) {

    public List<String> indicadoresOuVazio() {
        return indicadores == null ? List.of() : List.copyOf(indicadores);
    }

    public boolean vigenteOuFalso() {
        return Boolean.TRUE.equals(vigente);
    }
}
