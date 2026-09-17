package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.ApiTestSupport;
import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjetoControllerTests extends ApiTestSupport {

    @Autowired
    private OrientacaoRepository orientacaoRepository;

    @Test
    void gestorCadastraProjetoComResponsavelDoToken() throws Exception {
        mockMvc.perform(json(comToken(post("/api/projetos"), GESTOR), corpo("Retrofit da prensa 2", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsavelNome").value("Maria Silva"))
                .andExpect(jsonPath("$.status").value("NO_PRAZO"))
                .andExpect(jsonPath("$.etapa").value(1))
                .andExpect(jsonPath("$.totalEtapas").value(4))
                .andExpect(jsonPath("$.progresso").value(0))
                .andExpect(jsonPath("$.investimento").value(50000))
                .andExpect(jsonPath("$.atualizacoes", hasSize(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {OPERADOR, LIDERANCA})
    void apenasGestorCadastraProjeto(String email) throws Exception {
        mockMvc.perform(json(comToken(post("/api/projetos"), email), corpo("Projeto sem permissão", null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void operadorNaoConsultaProjetos() throws Exception {
        mockMvc.perform(comToken(get("/api/projetos"), OPERADOR))
                .andExpect(status().isForbidden());
    }

    @Test
    void liderancaConsultaProjetosEFiltra() throws Exception {
        mockMvc.perform(comToken(get("/api/projetos"), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(11))))
                .andExpect(jsonPath("$[*].nome", hasItem("Padronização de setup")));

        mockMvc.perform(comToken(get("/api/projetos?status=ATRASADO"), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("ATRASADO"))));
    }

    @Test
    void gestorAtualizaProjetoERegistraProgresso() throws Exception {
        String id = criar("Troca da esteira principal", null);

        mockMvc.perform(json(comToken(put("/api/projetos/" + id), GESTOR),
                        corpo("Troca da esteira principal - fase 1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Troca da esteira principal - fase 1"));

        mockMvc.perform(json(comToken(patch("/api/projetos/" + id + "/progresso"), GESTOR), """
                        {"etapa":2,"progresso":40,"status":"ATRASADO","observacao":"Aguardando peça importada"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapa").value(2))
                .andExpect(jsonPath("$.progresso").value(40))
                .andExpect(jsonPath("$.status").value("ATRASADO"))
                .andExpect(jsonPath("$.atualizacoes", hasSize(1)))
                .andExpect(jsonPath("$.atualizacoes[0].observacao").value("Aguardando peça importada"))
                .andExpect(jsonPath("$.atualizacoes[0].autorNome").value("Maria Silva"));
    }

    @Test
    void progressoAcimaDoTotalDeEtapasRetorna409() throws Exception {
        String id = criar("Projeto com poucas etapas", null);

        mockMvc.perform(json(comToken(patch("/api/projetos/" + id + "/progresso"), GESTOR), """
                        {"etapa":9,"progresso":10}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Etapa 9 excede o total de 4 etapas"));
    }

    @Test
    void progressoForaDaFaixaRetorna400() throws Exception {
        String id = criar("Projeto para validação", null);

        mockMvc.perform(json(comToken(patch("/api/projetos/" + id + "/progresso"), GESTOR), """
                        {"etapa":1,"progresso":140}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("progresso")));
    }

    @Test
    void gestorRegistraResultadosELiderancaConsulta() throws Exception {
        String id = criar("Eficiência energética", null);

        mockMvc.perform(json(comToken(patch("/api/projetos/" + id + "/resultados"), GESTOR), """
                        {"retornoFinanceiro":250000,"custoEvitado":30000,"aumentoProdutividade":7.5}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retornoFinanceiro").value(250000));

        mockMvc.perform(comToken(get("/api/projetos/" + id), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custoEvitado").value(30000))
                .andExpect(jsonPath("$.aumentoProdutividade").value(7.5));
    }

    @Test
    void projetoNasceDeIdeiaEmDecisaoEMarcaAIdeiaComoProjeto() throws Exception {
        String ideiaId = ideiaEmDecisao("Reaproveitar água de lavagem");

        String projetoId = criar("Reúso de água industrial", ideiaId);

        mockMvc.perform(comToken(get("/api/ideias/" + ideiaId), GESTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROJETO"));

        mockMvc.perform(comToken(get("/api/projetos/" + projetoId), GESTOR))
                .andExpect(jsonPath("$.ideiaOrigemId").value(ideiaId));

        mockMvc.perform(json(comToken(post("/api/projetos"), GESTOR), corpo("Segundo projeto da mesma ideia", ideiaId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Esta ideia já originou um projeto"));
    }

    @Test
    void ideiaAindaEnviadaNaoViraProjeto() throws Exception {
        String ideiaId = criarIdeia("Ideia recém enviada");

        mockMvc.perform(json(comToken(post("/api/projetos"), GESTOR), corpo("Projeto prematuro", ideiaId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A ideia precisa estar em DECISAO para virar projeto (status atual: ENVIADA)"));
    }

    @Test
    void prazoAnteriorAoInicioRetorna400() throws Exception {
        mockMvc.perform(json(comToken(post("/api/projetos"), GESTOR), """
                        {"nome":"Datas invertidas","orientacaoId":"%s","dataInicio":"2026-10-01",
                         "prazo":"2026-09-01","investimento":1000}
                        """.formatted(orientacaoId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].message", hasItem("O prazo não pode ser anterior à data de início")));
    }

    @Test
    void gestorExcluiProjeto() throws Exception {
        String id = criar("Projeto temporário", null);

        mockMvc.perform(comToken(delete("/api/projetos/" + id), GESTOR))
                .andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/api/projetos/" + id), GESTOR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Projeto não encontrado"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/projetos")).andExpect(status().isUnauthorized());
    }

    private String criar(String nome, String ideiaOrigemId) throws Exception {
        String body = mockMvc.perform(json(comToken(post("/api/projetos"), GESTOR), corpo(nome, ideiaOrigemId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String criarIdeia(String titulo) throws Exception {
        String body = mockMvc.perform(json(comToken(post("/api/ideias"), OPERADOR), """
                        {"titulo":"%s","categoria":"Operação","problemaObservado":"p","suaProposta":"s",
                         "orientacaoId":"%s"}
                        """.formatted(titulo, orientacaoId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String ideiaEmDecisao(String titulo) throws Exception {
        String id = criarIdeia(titulo);
        mudarStatusDaIdeia(id, "ANALISE").andExpect(status().isOk());
        mudarStatusDaIdeia(id, "DECISAO").andExpect(status().isOk());
        return id;
    }

    private ResultActions mudarStatusDaIdeia(String ideiaId, String status) throws Exception {
        return mockMvc.perform(json(comToken(patch("/api/ideias/" + ideiaId + "/status"), GESTOR),
                "{\"status\":\"%s\"}".formatted(status)));
    }

    private String corpo(String nome, String ideiaOrigemId) {
        String origem = ideiaOrigemId == null ? "" : "\"ideiaOrigemId\": \"%s\",".formatted(ideiaOrigemId);
        return """
                {
                  "nome": "%s",
                  "descricao": "Projeto criado no teste automatizado",
                  "orientacaoId": "%s",
                  %s
                  "dataInicio": "2026-09-01",
                  "prazo": "2027-03-31",
                  "investimento": 50000
                }
                """.formatted(nome, orientacaoId(), origem);
    }

    private String orientacaoId() {
        return orientacaoRepository.findByAtivoTrueOrderByCriadoEmDesc().getFirst().getId();
    }
}
