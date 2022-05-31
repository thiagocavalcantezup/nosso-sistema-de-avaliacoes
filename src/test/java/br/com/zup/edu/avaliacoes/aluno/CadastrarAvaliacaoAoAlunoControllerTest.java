package br.com.zup.edu.avaliacoes.aluno;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.zup.edu.avaliacoes.compartilhado.MensagemDeErro;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
public class CadastrarAvaliacaoAoAlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        avaliacaoRepository.deleteAll();
        alunoRepository.deleteAll();

        aluno = new Aluno("Thiago", "thiago@zup.edu", "Bootcamp Java");
        alunoRepository.save(aluno);
    }

    @Test
    void deveCadastrarUmaAvaliacaoComDadosValidos() throws Exception {
        // cenário (given)
        //
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        AvaliacaoRequest avaliacaoRequest = new AvaliacaoRequest(
            "Testes de Integração", "Avaliação A"
        );

        String payload = objectMapper.writeValueAsString(avaliacaoRequest);

        MockHttpServletRequestBuilder request = post(
            "/alunos/{id}/avaliacoes", aluno.getId()
        ).contentType(APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        // ação (when) e corretude (then)
        //
        mockMvc.perform(request)
               .andExpect(status().isCreated())
               .andExpect(redirectedUrlPattern(baseUrl + "/alunos/*/avaliacoes/*"));

        List<Avaliacao> avaliacoes = avaliacaoRepository.findAll();

        assertEquals(1, avaliacoes.size());
    }

    @Test
    void naoDeveCadastrarUmaAvaliacaoComDadosInvalidos() throws Exception {
        // cenário (given)
        //
        AvaliacaoRequest avaliacaoRequest = new AvaliacaoRequest(null, null);

        String payload = objectMapper.writeValueAsString(avaliacaoRequest);

        MockHttpServletRequestBuilder request = post(
            "/alunos/{id}/avaliacoes", aluno.getId()
        ).contentType(APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        // ação (when) e corretude (then)
        //
        String response = mockMvc.perform(request)
                                 .andExpect(status().isBadRequest())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(UTF_8);

        MensagemDeErro mensagemDeErro = objectMapper.readValue(response, MensagemDeErro.class);
        List<String> mensagens = mensagemDeErro.getMensagens();

        assertEquals(2, mensagens.size());
        assertThat(
            mensagens,
            containsInAnyOrder(
                "O campo titulo não deve estar em branco",
                "O campo avaliacaoReferente não deve estar em branco"
            )
        );
    }

    @Test
    void naoDeveCadastrarUmaAvaliacaoCasoOAlunoNaoExista() throws Exception {
        // cenário (given)
        //
        AvaliacaoRequest avaliacaoRequest = new AvaliacaoRequest(
            "Testes de Integração", "Avaliação A"
        );

        String payload = objectMapper.writeValueAsString(avaliacaoRequest);

        MockHttpServletRequestBuilder request = post("/alunos/{id}/avaliacoes", 1234).contentType(
            APPLICATION_JSON
        ).content(payload).header("Accept-Language", "pt-br");

        // ação (when) e corretude (then)
        //
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

}
