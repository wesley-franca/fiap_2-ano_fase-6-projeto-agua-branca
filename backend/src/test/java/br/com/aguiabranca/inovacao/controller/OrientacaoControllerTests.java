package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.ApiTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrientacaoControllerTests extends ApiTestSupport {

    @ParameterizedTest
    @ValueSource(strings = {OPERADOR, GESTOR, LIDERANCA})
    void qualquerPerfilAutenticadoListaOrientacoes(String email) throws Exception {
        mockMvc.perform(comToken(get("/api/orientacoes"), email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].titulo", hasItem("Zero acidentes")));
    }

    @Test
    void listaSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/orientacoes")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {OPERADOR, GESTOR})
    void perfilSemPermissaoNaoCriaOrientacao(String email) throws Exception {
        mockMvc.perform(json(comToken(post("/api/orientacoes"), email), corpo("Tentativa indevida", true)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void liderancaCriaOrientacaoComHistoricoDeCriacao() throws Exception {
        String id = criar("Reduzir retrabalho em 10%", true);

        mockMvc.perform(comToken(get("/api/orientacoes/" + id), OPERADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Reduzir retrabalho em 10%"))
                .andExpect(jsonPath("$.campanha").value("Campanha 2026"))
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.indicadores", contains("Retrabalho: 4%")));

        mockMvc.perform(comToken(get("/api/orientacoes/" + id + "/historico"), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].acao").value("CRIACAO"))
                .andExpect(jsonPath("$[0].alteradoPorNome").value("Paulo Andrade"));
    }

    @Test
    void atualizacaoAcumulaHistoricoMaisRecentePrimeiro() throws Exception {
        String id = criar("Ampliar coleta seletiva", true);

        mockMvc.perform(json(comToken(put("/api/orientacoes/" + id), LIDERANCA),
                        corpo("Ampliar coleta seletiva para 100% das áreas", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Ampliar coleta seletiva para 100% das áreas"))
                .andExpect(jsonPath("$.vigente").value(false));

        mockMvc.perform(comToken(get("/api/orientacoes/" + id + "/historico"), GESTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].acao").value("ATUALIZACAO"))
                .andExpect(jsonPath("$[1].acao").value("CRIACAO"));
    }

    @Test
    void exclusaoRemoveDaListaEMantemHistorico() throws Exception {
        String id = criar("Orientação temporária", true);

        mockMvc.perform(comToken(delete("/api/orientacoes/" + id), LIDERANCA))
                .andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/api/orientacoes"), LIDERANCA))
                .andExpect(jsonPath("$[*].titulo", not(hasItem("Orientação temporária"))));

        mockMvc.perform(comToken(get("/api/orientacoes/" + id), LIDERANCA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Orientação não encontrada"));

        mockMvc.perform(comToken(delete("/api/orientacoes/" + id), LIDERANCA))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtraPorVigenteEPorArea() throws Exception {
        String id = criar("Meta arquivada", false);

        mockMvc.perform(comToken(get("/api/orientacoes?vigente=false"), GESTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].vigente", everyItem(org.hamcrest.Matchers.is(false))))
                .andExpect(jsonPath("$[*].id", hasItem(id)));

        mockMvc.perform(comToken(get("/api/orientacoes?area=ehs"), OPERADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", contains("Zero acidentes")));
    }

    @Test
    void criacaoSemCamposObrigatoriosRetorna400() throws Exception {
        mockMvc.perform(json(comToken(post("/api/orientacoes"), LIDERANCA), """
                        {"titulo": "", "descricao": "", "categoria": "", "campanha": "", "area": "", "periodo": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("titulo")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("periodo")));
    }

    @Test
    void corpoSemVigenteCriaOrientacaoNaoVigente() throws Exception {
        mockMvc.perform(json(comToken(post("/api/orientacoes"), LIDERANCA), """
                        {
                          "titulo": "Diretriz sem campo vigente",
                          "descricao": "O app pode omitir campos opcionais",
                          "categoria": "Qualidade",
                          "campanha": "Campanha 2026",
                          "area": "Qualidade",
                          "periodo": "2º semestre 2026"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vigente").value(false))
                .andExpect(jsonPath("$.indicadores", hasSize(0)));
    }

    @Test
    void idInexistenteRetorna404() throws Exception {
        mockMvc.perform(comToken(get("/api/orientacoes/64b7f0c2a1b2c3d4e5f60000"), OPERADOR))
                .andExpect(status().isNotFound());
    }

    private String criar(String titulo, boolean vigente) throws Exception {
        String body = mockMvc.perform(json(comToken(post("/api/orientacoes"), LIDERANCA), corpo(titulo, vigente)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static String corpo(String titulo, boolean vigente) {
        return """
                {
                  "titulo": "%s",
                  "descricao": "Diretriz criada no teste automatizado",
                  "categoria": "Qualidade",
                  "campanha": "Campanha 2026",
                  "area": "Qualidade",
                  "periodo": "2º semestre 2026",
                  "indicadores": ["Retrabalho: 4%%"],
                  "vigente": %s
                }
                """.formatted(titulo, vigente);
    }
}
