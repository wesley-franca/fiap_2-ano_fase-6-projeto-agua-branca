package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.domain.PrioridadeIdeia;
import br.com.aguiabranca.inovacao.domain.StatusIdeia;
import br.com.aguiabranca.inovacao.dto.ideia.IdeiaRequest;
import br.com.aguiabranca.inovacao.dto.ideia.IdeiaResponse;
import br.com.aguiabranca.inovacao.dto.ideia.PrioridadeRequest;
import br.com.aguiabranca.inovacao.dto.ideia.StatusIdeiaRequest;
import br.com.aguiabranca.inovacao.security.UsuarioAutenticado;
import br.com.aguiabranca.inovacao.service.IdeiaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/ideias")
@RequiredArgsConstructor
@Tag(name = "Ideias de inovação")
public class IdeiaController {

    private final IdeiaService service;

    @GetMapping
    @Operation(summary = "Lista ideias: operador vê apenas as próprias; gestor e liderança veem todas")
    public List<IdeiaResponse> listar(@AuthenticationPrincipal Jwt jwt,
                                      @RequestParam(required = false) StatusIdeia status,
                                      @RequestParam(required = false) PrioridadeIdeia prioridade,
                                      @RequestParam(required = false) String area,
                                      @RequestParam(required = false) String orientacaoId) {
        return service.listar(UsuarioAutenticado.de(jwt), status, prioridade, area, orientacaoId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma ideia (operador apenas as próprias)")
    public IdeiaResponse buscar(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return service.buscar(id, UsuarioAutenticado.de(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Cadastra uma ideia vinculada a uma orientação (apenas operador)")
    public IdeiaResponse criar(@Valid @RequestBody IdeiaRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, UsuarioAutenticado.de(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Edita a própria ideia enquanto ela estiver como ENVIADA")
    public IdeiaResponse atualizar(@PathVariable String id, @Valid @RequestBody IdeiaRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, request, UsuarioAutenticado.de(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Exclui a própria ideia enquanto ela estiver como ENVIADA")
    public void excluir(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, UsuarioAutenticado.de(jwt));
    }

    @PatchMapping("/{id}/prioridade")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Define a prioridade da ideia (apenas gestor)")
    public IdeiaResponse priorizar(@PathVariable String id, @Valid @RequestBody PrioridadeRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.priorizar(id, request, UsuarioAutenticado.de(jwt));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Avança, aprova ou rejeita a ideia (apenas gestor)")
    public IdeiaResponse alterarStatus(@PathVariable String id, @Valid @RequestBody StatusIdeiaRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return service.alterarStatus(id, request, UsuarioAutenticado.de(jwt));
    }
}
