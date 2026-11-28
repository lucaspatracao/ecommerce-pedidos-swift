package com.ecommerce.pedidos.integracao;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Status HTTP da API (Aula 15): todo POST que cria responde 201 com
 * {@code Location} apontando para o recurso, e todo DELETE responde 204 sem corpo.
 *
 * <p>Além de conferir o cabeçalho, cada teste faz um GET no endereço devolvido:
 * o {@code Location} precisa levar a um recurso que existe de verdade.</p>
 */
@Tag("integracao")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatusHttpApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private MvcResult criar(String url, String corpo, String locationEsperado) throws Exception {
        MvcResult resultado = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, endsWith(locationEsperado)))
            .andReturn();
        String location = resultado.getResponse().getHeader(HttpHeaders.LOCATION);
        mvc.perform(get(location)).andExpect(status().isOk());
        return resultado;
    }

    private String novoCliente() throws Exception {
        String cpf = DadosDeTeste.novoCpf();
        criar("/api/clientes", """
            {"nome":"Rui Lima","cpf":"%1$s","email":"rui%1$s@email.com","telefone":"11999997777","endereco":"Rua B, 2"}
            """.formatted(cpf), "/api/clientes/" + cpf);
        return cpf;
    }

    private String novoProduto() throws Exception {
        String codigo = DadosDeTeste.novoCodigo("P-HTTP");
        criar("/api/produtos", """
            {"codigo":"%s","nome":"Produto HTTP","descricao":"","preco":"20.00","estoque":10,"disponivel":true}
            """.formatted(codigo), "/api/produtos/" + codigo);
        return codigo;
    }

    private JsonNode novoPedido(String cpf, String codigo) throws Exception {
        String outro = novoProduto();
        MvcResult resultado = mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content("""
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":1},{"codigo":"%s","quantidade":1}]}
                """.formatted(cpf, codigo, outro)))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode pedido = json.readTree(resultado.getResponse().getContentAsString());
        String location = resultado.getResponse().getHeader(HttpHeaders.LOCATION);
        org.junit.jupiter.api.Assertions.assertNotNull(location, "POST /api/pedidos sem Location");
        org.junit.jupiter.api.Assertions.assertTrue(location.endsWith("/api/pedidos/" + pedido.get("numero").asText()));
        mvc.perform(get(location)).andExpect(status().isOk());
        return pedido;
    }

    @Test
    @DisplayName("POST /api/clientes → 201 + Location /api/clientes/{cpf} + DTO no corpo")
    void postCliente() throws Exception {
        String cpf = DadosDeTeste.novoCpf();
        criar("/api/clientes", """
            {"nome":"Ana Reis","cpf":"%1$s","email":"ana%1$s@email.com","telefone":"11999996666","endereco":"Rua C, 3"}
            """.formatted(cpf), "/api/clientes/" + cpf);
    }

    @Test
    @DisplayName("POST /api/produtos → 201 + Location /api/produtos/{codigo}")
    void postProduto() throws Exception {
        novoProduto();
    }

    @Test
    @DisplayName("POST /api/pedidos → 201 + Location /api/pedidos/{numero}")
    void postPedido() throws Exception {
        novoPedido(novoCliente(), novoProduto());
    }

    @Test
    @DisplayName("POST /api/pedidos/{numero}/pagamento → 201 + Location do sub-recurso + pagamento no corpo")
    void postPagamento() throws Exception {
        String numero = novoPedido(novoCliente(), novoProduto()).get("numero").asText();
        MvcResult resultado = criar("/api/pedidos/" + numero + "/pagamento", """
            {"tipo":"PIX","valor":"40.00","chave":"rui@email.com","tipoChave":"EMAIL"}
            """, "/api/pedidos/" + numero + "/pagamento");
        org.junit.jupiter.api.Assertions.assertTrue(resultado.getResponse().getContentAsString().contains("\"descricao\":\"Pix\""));
    }

    @Test
    @DisplayName("DELETE de cliente, produto, item e pedido → 204 sem corpo")
    void deletesRespondem204() throws Exception {
        String cpfSemPedido = novoCliente();
        mvc.perform(delete("/api/clientes/{cpf}", cpfSemPedido))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        String produtoAvulso = novoProduto();
        mvc.perform(delete("/api/produtos/{codigo}", produtoAvulso))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        JsonNode pedido = novoPedido(novoCliente(), novoProduto());
        String numero = pedido.get("numero").asText();
        long item = pedido.get("itens").get(0).get("id").asLong();
        mvc.perform(delete("/api/pedidos/{numero}/itens/{id}", numero, item))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        mvc.perform(get("/api/pedidos/{numero}", numero)).andExpect(jsonPath("$.itens.length()").value(1));

        mvc.perform(delete("/api/pedidos/{numero}", numero))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }
}
