package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.ApiTestSupport;
import br.com.aguiabranca.inovacao.domain.Projeto;
import br.com.aguiabranca.inovacao.domain.StatusProjeto;
import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;
import br.com.aguiabranca.inovacao.repository.ProjetoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerTests extends ApiTestSupport {

    @Autowired
    private ProjetoRepository projetoRepository;
    @Autowired
    private OrientacaoRepository orientacaoRepository;

    @Test
    void resumoDaLiderancaConsolidaOsProjetos() throws Exception {
        List<Projeto> projetos = projetoRepository.findAll();
        BigDecimal investimento = soma(projetos, Projeto::getInvestimento);
        BigDecimal retorno = soma(projetos, Projeto::getRetornoFinanceiro);
        BigDecimal lucro = retorno.subtract(investimento);
        BigDecimal roi = lucro.divide(investimento, 2, RoundingMode.HALF_UP);
        long noPrazo = projetos.stream().filter(p -> p.getStatus() == StatusProjeto.NO_PRAZO).count();

        mockMvc.perform(comToken(get("/api/dashboard/resumo"), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicadores.totalProjetos").value(projetos.size()))
                .andExpect(jsonPath("$.indicadores.projetosNoPrazo").value(noPrazo))
                .andExpect(jsonPath("$.indicadores.investimentoTotal").value(investimento.doubleValue()))
                .andExpect(jsonPath("$.indicadores.retornoTotal").value(retorno.doubleValue()))
                .andExpect(jsonPath("$.indicadores.lucro").value(lucro.doubleValue()))
                .andExpect(jsonPath("$.indicadores.roi").value(roi.doubleValue()))
                .andExpect(jsonPath("$.indicadores.custoEvitadoTotal").value(
                        soma(projetos, Projeto::getCustoEvitado).doubleValue()))
                .andExpect(jsonPath("$.projetosPorStatus", hasKey("NO_PRAZO")))
                .andExpect(jsonPath("$.projetosPorStatus.NO_PRAZO").value(noPrazo))
                .andExpect(jsonPath("$.ideiasPorStatus", hasKey("ENVIADA")))
                .andExpect(jsonPath("$.porOrientacao.length()", greaterThanOrEqualTo(3)));
    }

    @Test
    void resumoTrazSeriePorOrientacaoOrdenadaPorLucro() throws Exception {
        String body = mockMvc.perform(comToken(get("/api/dashboard/resumo"), LIDERANCA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Double> lucros = com.jayway.jsonpath.JsonPath.read(body, "$.porOrientacao[*].indicadores.lucro");
        assertOrdenadoDesc(lucros);
    }

    @Test
    void consultaPorOrientacaoSomaApenasOsProjetosDela() throws Exception {
        String orientacaoId = orientacaoRepository.findByAtivoTrueOrderByCriadoEmDesc().getFirst().getId();
        List<Projeto> daOrientacao = projetoRepository.findAll().stream()
                .filter(p -> orientacaoId.equals(p.getOrientacaoId()))
                .toList();

        mockMvc.perform(comToken(get("/api/dashboard/orientacoes/" + orientacaoId), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orientacaoId").value(orientacaoId))
                .andExpect(jsonPath("$.indicadores.totalProjetos").value(daOrientacao.size()))
                .andExpect(jsonPath("$.indicadores.investimentoTotal").value(
                        soma(daOrientacao, Projeto::getInvestimento).doubleValue()));
    }

    @Test
    void consultaPorProjetoTrazRetornoELucro() throws Exception {
        Projeto projeto = projetoRepository.findAll().stream()
                .filter(p -> p.getRetornoFinanceiro() != null && p.getInvestimento() != null)
                .findFirst()
                .orElseThrow();
        BigDecimal lucro = projeto.getRetornoFinanceiro().subtract(projeto.getInvestimento());

        mockMvc.perform(comToken(get("/api/dashboard/projetos/" + projeto.getId()), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(projeto.getNome()))
                .andExpect(jsonPath("$.lucro").value(lucro.doubleValue()))
                .andExpect(jsonPath("$.orientacaoTitulo").isNotEmpty());
    }

    @Test
    void painelDoGestorContaIdeiasEProjetos() throws Exception {
        mockMvc.perform(comToken(get("/api/dashboard/gestor"), GESTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ideiasNovas").isNumber())
                .andExpect(jsonPath("$.ideiasEmAnalise").isNumber())
                .andExpect(jsonPath("$.projetosAtivos").isNumber())
                .andExpect(jsonPath("$.projetosAtrasados", greaterThanOrEqualTo(2)));
    }

    @ParameterizedTest
    @ValueSource(strings = {OPERADOR, GESTOR})
    void resumoExecutivoESoDaLideranca(String email) throws Exception {
        mockMvc.perform(comToken(get("/api/dashboard/resumo"), email))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(get("/api/dashboard/orientacoes"), email))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {OPERADOR, LIDERANCA})
    void painelDoGestorESoDoGestor(String email) throws Exception {
        mockMvc.perform(comToken(get("/api/dashboard/gestor"), email))
                .andExpect(status().isForbidden());
    }

    @Test
    void idInexistenteRetorna404() throws Exception {
        mockMvc.perform(comToken(get("/api/dashboard/projetos/64b7f0c2a1b2c3d4e5f60000"), LIDERANCA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Projeto não encontrado"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo")).andExpect(status().isUnauthorized());
    }

    private static void assertOrdenadoDesc(List<Double> valores) {
        for (int i = 1; i < valores.size(); i++) {
            if (valores.get(i - 1) < valores.get(i)) {
                throw new AssertionError("Série por orientação não está ordenada por lucro: " + valores);
            }
        }
    }

    private static BigDecimal soma(List<Projeto> projetos, Function<Projeto, BigDecimal> campo) {
        return projetos.stream()
                .map(campo)
                .map(v -> v == null ? BigDecimal.ZERO : v)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
