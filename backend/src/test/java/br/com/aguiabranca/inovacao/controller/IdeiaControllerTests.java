package br.com.aguiabranca.inovacao.controller;

import br.com.aguiabranca.inovacao.ApiTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.aguiabranca.inovacao.repository.OrientacaoRepository;

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

class IdeiaControllerTests extends ApiTestSupport {

    @Autowired
    private OrientacaoRepository orientacaoRepository;

    @Test
    void operadorCadastraIdeiaComAutoriaVindaDoToken() throws Exception {
        mockMvc.perform(json(comToken(post("/api/ideias"), OPERADOR), corpo("Trocar filtros da bomba 4")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ENVIADA"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.impacto").value("MEDIO"))
                .andExpect(jsonPath("$.nomeOperador").value("João Costa"))
                .andExpect(jsonPath("$.area").value("Manutenção"))
                .andExpect(jsonPath("$.orientacaoId").value(orientacaoId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {GESTOR, LIDERANCA})
    void apenasOperadorCadastraIdeia(String email) throws Exception {
        mockMvc.perform(json(comToken(post("/api/ideias"), email), corpo("Ideia de perfil sem permissão")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ideiaPrecisaDeOrientacaoValida() throws Exception {
        mockMvc.perform(json(comToken(post("/api/ideias"), OPERADOR), """
                        {"titulo":"Sem vínculo","categoria":"Manutenção","problemaObservado":"p","suaProposta":"s"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("orientacaoId")));

        mockMvc.perform(json(comToken(post("/api/ideias"), OPERADOR), """
                        {"titulo":"Vínculo inexistente","categoria":"Manutenção","problemaObservado":"p",
                         "suaProposta":"s","orientacaoId":"64b7f0c2a1b2c3d4e5f60000"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Orientação não encontrada"));
    }

    @Test
    void operadorVeApenasAsProprias() throws Exception {
        String id = criar(OPERADOR, "Ideia do João");
        criar(OPERADORA_ANA, "Ideia da Ana");

        mockMvc.perform(comToken(get("/api/ideias"), OPERADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nomeOperador", everyItem(is("João Costa"))))
                .andExpect(jsonPath("$[*].id", hasItem(id)));

        mockMvc.perform(comToken(get("/api/ideias"), GESTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$[*].nomeOperador", hasItem("Ana Lima")));
    }

    @Test
    void operadorNaoAcessaIdeiaDeOutro() throws Exception {
        String id = criar(OPERADORA_ANA, "Ideia privada da Ana");

        mockMvc.perform(comToken(get("/api/ideias/" + id), OPERADOR))
                .andExpect(status().isForbidden());

        mockMvc.perform(comToken(get("/api/ideias/" + id), GESTOR))
                .andExpect(status().isOk());
    }

    @Test
    void autorEditaEExcluiEnquantoEnviada() throws Exception {
        String id = criar(OPERADOR, "Rever rota de inspeção");

        mockMvc.perform(json(comToken(put("/api/ideias/" + id), OPERADOR), corpo("Rever rota de inspeção noturna")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Rever rota de inspeção noturna"));

        mockMvc.perform(json(comToken(put("/api/ideias/" + id), OPERADORA_ANA), corpo("Sequestro de ideia")))
                .andExpect(status().isForbidden());

        mockMvc.perform(comToken(delete("/api/ideias/" + id), OPERADOR))
                .andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/api/ideias/" + id), GESTOR))
                .andExpect(status().isNotFound());
    }

    @Test
    void autorNaoEditaDepoisQueGestorMoveOStatus() throws Exception {
        String id = criar(OPERADOR, "Lubrificação da esteira 7");
        mudarStatus(id, "ANALISE", null).andExpect(status().isOk());

        mockMvc.perform(json(comToken(put("/api/ideias/" + id), OPERADOR), corpo("Tentativa tardia")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(comToken(delete("/api/ideias/" + id), OPERADOR))
                .andExpect(status().isConflict());
    }

    @Test
    void gestorPriorizaEOperadorNao() throws Exception {
        String id = criar(OPERADOR, "Sensor de temperatura no forno");

        mockMvc.perform(json(comToken(patch("/api/ideias/" + id + "/prioridade"), GESTOR), """
                        {"prioridade":"ALTA"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridade").value("ALTA"));

        mockMvc.perform(json(comToken(patch("/api/ideias/" + id + "/prioridade"), OPERADOR), """
                        {"prioridade":"BAIXA"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void fluxoDeAprovacaoSegueAsEtapas() throws Exception {
        String id = criar(OPERADOR, "Automatizar apontamento de produção");

        mudarStatus(id, "ANALISE", null).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ANALISE"));
        mudarStatus(id, "DECISAO", null).andExpect(status().isOk());
        mudarStatus(id, "PROJETO", "Aprovada para virar projeto").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROJETO"))
                .andExpect(jsonPath("$.comentarioAvaliacao").value("Aprovada para virar projeto"));

        mudarStatus(id, "ANALISE", null).andExpect(status().isConflict());
    }

    @Test
    void transicaoDiretaParaProjetoNaoEPermitida() throws Exception {
        String id = criar(OPERADOR, "Pular etapas do fluxo");

        mudarStatus(id, "PROJETO", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Transição de status inválida: ENVIADA → PROJETO"));
    }

    @Test
    void rejeicaoExigeComentario() throws Exception {
        String id = criar(OPERADOR, "Ideia que será rejeitada");

        mudarStatus(id, "REJEITADA", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Informe o motivo da rejeição no campo comentario"));

        mudarStatus(id, "REJEITADA", "Fora do escopo do trimestre")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarioAvaliacao").value("Fora do escopo do trimestre"));
    }

    @Test
    void filtraPorStatusEPrioridade() throws Exception {
        mockMvc.perform(comToken(get("/api/ideias?status=ENVIADA"), GESTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("ENVIADA"))));

        mockMvc.perform(comToken(get("/api/ideias?prioridade=ALTA"), LIDERANCA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].prioridade", everyItem(is("ALTA"))));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/ideias")).andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions mudarStatus(String id, String status, String comentario)
            throws Exception {
        String corpo = comentario == null
                ? "{\"status\":\"%s\"}".formatted(status)
                : "{\"status\":\"%s\",\"comentario\":\"%s\"}".formatted(status, comentario);
        return mockMvc.perform(json(comToken(patch("/api/ideias/" + id + "/status"), GESTOR), corpo));
    }

    private String criar(String email, String titulo) throws Exception {
        String body = mockMvc.perform(json(comToken(post("/api/ideias"), email), corpo(titulo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String corpo(String titulo) {
        return """
                {
                  "titulo": "%s",
                  "categoria": "Manutenção",
                  "problemaObservado": "Problema observado no teste",
                  "suaProposta": "Proposta enviada no teste",
                  "orientacaoId": "%s"
                }
                """.formatted(titulo, orientacaoId());
    }

    private String orientacaoId() {
        return orientacaoRepository.findByAtivoTrueOrderByCriadoEmDesc().getFirst().getId();
    }
}
