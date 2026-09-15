package br.com.aguiabranca.inovacao.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "operador@aguiabranca.com")
        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(example = "senha123")
        @NotBlank(message = "Informe a senha")
        String senha
) {

    /** Remove espaços do e-mail (comuns em teclados móveis) antes da validação. */
    public LoginRequest {
        email = email == null ? null : email.strip();
    }
}
