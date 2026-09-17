package br.com.aguiabranca.inovacao.security;

import br.com.aguiabranca.inovacao.domain.Role;
import org.springframework.security.oauth2.jwt.Jwt;

/** Dados do usuário extraídos do token, usados para autoria e regras de dono. */
public record UsuarioAutenticado(String id, String nome, Role role) {

    public static UsuarioAutenticado de(Jwt jwt) {
        return new UsuarioAutenticado(
                jwt.getSubject(),
                jwt.getClaimAsString("nome"),
                Role.valueOf(jwt.getClaimAsString(JwtConfig.ROLE_CLAIM)));
    }
}
