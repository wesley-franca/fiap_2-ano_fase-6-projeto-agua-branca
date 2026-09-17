package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.dto.dashboard.PainelGestorResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.ResumoLiderancaResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.ResumoOrientacaoResponse;
import br.com.aguiabranca.inovacao.dto.dashboard.ResumoProjetoResponse;
import br.com.aguiabranca.inovacao.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboards e relatórios")
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/gestor")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "KPIs do painel do gestor")
    public PainelGestorResponse painelDoGestor() {
        return service.painelDoGestor();
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Visão executiva: indicadores gerais e séries para gráficos")
    public ResumoLiderancaResponse resumo() {
        return service.resumoDaLideranca();
    }

    @GetMapping("/orientacoes")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Resultado consolidado por orientação estratégica")
    public List<ResumoOrientacaoResponse> porOrientacao() {
        return service.porOrientacao();
    }

    @GetMapping("/orientacoes/{id}")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Resultado de uma orientação específica")
    public ResumoOrientacaoResponse porOrientacao(@PathVariable String id) {
        return service.porOrientacao(id);
    }

    @GetMapping("/projetos/{id}")
    @PreAuthorize("hasRole('LIDERANCA')")
    @Operation(summary = "Retorno detalhado de um projeto")
    public ResumoProjetoResponse porProjeto(@PathVariable String id) {
        return service.porProjeto(id);
    }
}
