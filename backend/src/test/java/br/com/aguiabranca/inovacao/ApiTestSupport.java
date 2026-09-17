package br.com.aguiabranca.inovacao;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Base dos testes de API: sobe o contexto com MongoDB em container e autentica os perfis do seed. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class ApiTestSupport {

    protected static final String OPERADOR = "operador@aguiabranca.com";
    protected static final String GESTOR = "gestor@aguiabranca.com";
    protected static final String LIDERANCA = "lideranca@aguiabranca.com";

    @Autowired
    protected MockMvc mockMvc;

    protected String token(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "senha": "senha123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder request, String email) throws Exception {
        return request.header("Authorization", "Bearer " + token(email));
    }

    protected MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
