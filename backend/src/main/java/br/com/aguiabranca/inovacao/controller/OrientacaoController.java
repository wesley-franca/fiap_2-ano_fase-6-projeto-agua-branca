package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.dto.orientacao.HistoricoResponse;
import br.com.aguiabranca.inovacao.dto.orientacao.OrientacaoRequest;
import br.com.aguiabranca.inovacao.dto.orientacao.OrientacaoResponse;
import br.com.aguiabranca.inovacao.security.UsuarioAutenticado;
import br.com.aguiabranca.inovacao.service.OrientacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orientacoes")
@RequiredArgsConstructor
@Tag(name = "Orientações estratégicas")
public class OrientacaoController {

    private final OrientacaoService service;

    @GetMapping
    @Operation(summary = "Lista as orientações estratégicas (todos os perfis)")
    public List<OrientacaoResponse> listar(@RequestParam(required = false) Boolean vigente,
                                           @RequestParam(required = false) String area) {
        return service.listar(vigente, area);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma orientação (todos os perfis)")
    public OrientacaoResponse buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Histórico de alterações da orientação (todos os perfis)")
    public List<HistoricoResponse> historico(@PathVariable String id) {
        return service.historico(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Cria uma orientação (apenas liderança)")
    public OrientacaoResponse criar(@Valid @RequestBody OrientacaoRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, UsuarioAutenticado.de(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Atualiza uma orientação (apenas liderança)")
    public OrientacaoResponse atualizar(@PathVariable String id,
                                        @Valid @RequestBody OrientacaoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, request, UsuarioAutenticado.de(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Exclui (logicamente) uma orientação (apenas liderança)")
    public void excluir(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, UsuarioAutenticado.de(jwt));
    }
}
