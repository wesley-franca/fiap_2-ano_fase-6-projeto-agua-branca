package br.com.aguiabranca.inovacao.service;

import br.com.aguiabranca.inovacao.domain.Usuario;
import br.com.aguiabranca.inovacao.dto.auth.LoginRequest;
import br.com.aguiabranca.inovacao.dto.auth.LoginResponse;
import br.com.aguiabranca.inovacao.dto.auth.UsuarioResponse;
import br.com.aguiabranca.inovacao.exception.CredenciaisInvalidasException;
import br.com.aguiabranca.inovacao.repository.UsuarioRepository;
import br.com.aguiabranca.inovacao.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /** Usado quando o e-mail não existe, para que a resposta leve o mesmo tempo de um e-mail válido. */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.hashFicticio = passwordEncoder.encode("senha-inexistente");
    }

    public LoginResponse login(LoginRequest request) {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(normalizarEmail(request.email()));
        String hash = encontrado.map(Usuario::getSenhaHash).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);

        Usuario usuario = encontrado
                .filter(u -> senhaConfere && u.isAtivo())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));

        TokenService.TokenGerado token = tokenService.gerar(usuario);
        return new LoginResponse(token.token(), "Bearer", token.expiraEm(), UsuarioResponse.from(usuario));
    }

    public UsuarioResponse usuarioAtual(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .filter(Usuario::isAtivo)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new CredenciaisInvalidasException("Usuário não encontrado ou inativo"));
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
