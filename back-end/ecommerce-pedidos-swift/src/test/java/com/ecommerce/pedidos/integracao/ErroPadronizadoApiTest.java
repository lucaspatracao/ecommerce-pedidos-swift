package com.ecommerce.pedidos.integracao;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.ecommerce.pedidos.servico.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Corpo de erro padronizado (Aula 15): {@code status}, {@code erro},
 * {@code mensagem}, {@code caminho} e, só na validação, {@code campos}.
 *
 * <p>O {@code DashboardService} é trocado por um mock para provocar um erro
 * inesperado de verdade (500) e provar que o corpo não vaza detalhes internos.</p>
 */
@Tag("integracao")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErroPadronizadoApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DashboardService dashboardService;

    private ResultActions enviar(String url, String corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String novoCliente() throws Exception {
        String cpf = DadosDeTeste.novoCpf();
        enviar("/api/clientes", """
            {"nome":"Eva Melo","cpf":"%1$s","email":"eva%1$s@email.com","telefone":"11999995555","endereco":"Rua D, 4"}
            """.formatted(cpf)).andExpect(status().isCreated());
        return cpf;
    }

    @Test
    @DisplayName("400 de validação: corpo completo com a lista de campos")
    void validacaoTrazCampos() throws Exception {
        enviar("/api/clientes", """
            {"nome":"","cpf":"%s","email":"sem-arroba","telefone":"11999995555","endereco":"Rua D, 4"}
            """.formatted(DadosDeTeste.novoCpf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.erro").value("Requisição inválida"))
            .andExpect(jsonPath("$.mensagem").value("Há campos inválidos. Corrija-os e tente novamente."))
            .andExpect(jsonPath("$.caminho").value("/api/clientes"))
            .andExpect(jsonPath("$.campos[*].campo", hasItem("email")))
            .andExpect(jsonPath("$.campos[*].campo", hasItem("nome")))
            .andExpect(jsonPath("$.campos[?(@.campo == 'email')].mensagem").value(hasItem("E-mail inválido.")));
    }

    @Test
    @DisplayName("400 de JSON malformado: sem a lista de campos")
    void jsonMalformado() throws Exception {
        enviar("/api/produtos", "{ isto não é json")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.erro").value("Requisição inválida"))
            .andExpect(jsonPath("$.mensagem").value("Corpo da requisição inválido ou mal formatado."))
            .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    @DisplayName("404: um handler só para cliente, produto, pedido e pagamento")
    void naoEncontrado() throws Exception {
        mvc.perform(get("/api/produtos/{codigo}", "NAO-EXISTE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.erro").value("Não encontrado"))
            .andExpect(jsonPath("$.mensagem").value("Produto não encontrado: NAO-EXISTE"))
            .andExpect(jsonPath("$.caminho").value("/api/produtos/NAO-EXISTE"))
            .andExpect(jsonPath("$.campos").doesNotExist());
        mvc.perform(get("/api/pedidos/{numero}/pagamento", "PED-0000-00000"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.erro").value("Não encontrado"));
    }

    @Test
    @DisplayName("409: e-mail já cadastrado, estoque insuficiente e pedido já pago")
    void conflitos() throws Exception {
        String cpf = novoCliente();
        enviar("/api/clientes", """
            {"nome":"Outra","cpf":"%s","email":"eva%s@email.com","telefone":"11999995555","endereco":"Rua D, 4"}
            """.formatted(DadosDeTeste.novoCpf(), cpf))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.erro").value("Conflito"))
            .andExpect(jsonPath("$.mensagem").value(containsString("e-mail")));

        String codigo = DadosDeTeste.novoCodigo("P-ERR");
        enviar("/api/produtos", """
            {"codigo":"%s","nome":"Caneca","descricao":"","preco":"15.00","estoque":2,"disponivel":true}
            """.formatted(codigo)).andExpect(status().isCreated());
        enviar("/api/pedidos", """
            {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":5}]}
            """.formatted(cpf, codigo))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensagem").value("Estoque insuficiente de Caneca: disponível 2, solicitado 5"));

        String numero = com.jayway.jsonpath.JsonPath.read(enviar("/api/pedidos", """
            {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":1}]}
            """.formatted(cpf, codigo)).andReturn().getResponse().getContentAsString(), "$.numero");
        String pix = "{\"tipo\":\"PIX\",\"valor\":\"15.00\",\"chave\":\"eva@email.com\",\"tipoChave\":\"EMAIL\"}";
        enviar("/api/pedidos/" + numero + "/pagamento", pix).andExpect(status().isCreated());
        enviar("/api/pedidos/" + numero + "/pagamento", pix)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensagem").value("Pedido " + numero + " já está pago e não pode ser pago de novo."));
    }

    @Test
    @DisplayName("500: mensagem genérica, sem nome de classe, SQL ou stack trace no corpo")
    void erroInternoNaoVaza() throws Exception {
        when(dashboardService.gerarResumo()).thenThrow(new IllegalMonitorStateException(
            "org.hibernate.exception.SQLGrammarException: select * from cliente where senha = 'x'"));

        mvc.perform(get("/api/dashboard"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.erro").value("Erro interno"))
            .andExpect(jsonPath("$.mensagem").value("Erro interno. Tente novamente mais tarde."))
            .andExpect(jsonPath("$.caminho").value("/api/dashboard"))
            .andExpect(jsonPath("$.trace").doesNotExist())
            .andExpect(content().string(not(containsString("Exception"))))
            .andExpect(content().string(not(containsString("org.hibernate"))))
            .andExpect(content().string(not(containsString("select"))))
            .andExpect(content().string(not(containsString("at com.ecommerce"))));
    }
}
