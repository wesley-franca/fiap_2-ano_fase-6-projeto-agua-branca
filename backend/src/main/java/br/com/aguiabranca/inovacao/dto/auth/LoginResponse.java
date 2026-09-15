package br.com.aguiabranca.inovacao.dto.auth;

import java.time.Instant;

public record LoginResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) {
}
