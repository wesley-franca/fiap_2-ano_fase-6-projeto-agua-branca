package br.com.aguiabranca.inovacao.dto.auth;

import br.com.aguiabranca.inovacao.domain.Role;
import br.com.aguiabranca.inovacao.domain.Usuario;

public record UsuarioResponse(String id, String nome, String email, Role role, String area) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(), usuario.getArea());
    }
}
