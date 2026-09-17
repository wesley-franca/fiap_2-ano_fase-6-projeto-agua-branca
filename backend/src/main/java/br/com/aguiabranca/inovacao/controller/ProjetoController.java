package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import br.com.aguiabranca.inovacao.dto.projeto.ProgressoRequest;
import br.com.aguiabranca.inovacao.dto.projeto.ProjetoRequest;
import br.com.aguiabranca.inovacao.dto.projeto.ProjetoResponse;
import br.com.aguiabranca.inovacao.dto.projeto.ResultadosRequest;
import br.com.aguiabranca.inovacao.security.UsuarioAutenticado;
import br.com.aguiabranca.inovacao.service.ProjetoService;
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
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos e iniciativas")
public class ProjetoController {

    private final ProjetoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    @Operation(summary = "Lista os projetos (gestor e liderança)")
    public List<ProjetoResponse> listar(@RequestParam(required = false) StatusProjeto status,
                                        @RequestParam(required = false) String orientacaoId) {
        return service.listar(status, orientacaoId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDERANCA')")
    @Operation(summary = "Detalha um projeto, com o histórico de atualizações")
    public ProjetoResponse buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Cadastra um projeto, opcionalmente a partir de uma ideia em DECISAO")
    public ProjetoResponse criar(@Valid @RequestBody ProjetoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, UsuarioAutenticado.de(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Atualiza os dados do projeto (apenas gestor)")
    public ProjetoResponse atualizar(@PathVariable String id, @Valid @RequestBody ProjetoRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, request, UsuarioAutenticado.de(jwt));
    }

    @PatchMapping("/{id}/progresso")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Atualiza etapa, progresso e status, registrando o acompanhamento")
    public ProjetoResponse registrarProgresso(@PathVariable String id, @Valid @RequestBody ProgressoRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return service.registrarProgresso(id, request, UsuarioAutenticado.de(jwt));
    }

    @PatchMapping("/{id}/resultados")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Registra os resultados obtidos (retorno, custo evitado e produtividade)")
    public ProjetoResponse registrarResultados(@PathVariable String id, @Valid @RequestBody ResultadosRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return service.registrarResultados(id, request, UsuarioAutenticado.de(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Exclui um projeto (apenas gestor)")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
