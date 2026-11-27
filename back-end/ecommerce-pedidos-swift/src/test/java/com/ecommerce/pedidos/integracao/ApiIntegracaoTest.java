package com.ecommerce.pedidos.integracao;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Testes de ponta a ponta da API REST: requisição HTTP simulada (MockMvc)
 * → controller → serviço → banco H2 → JSON de resposta.
 *
 * <p>Conferem o contrato com o front-end: rotas, status HTTP, nomes dos
 * campos JSON e o corpo de erro {@code { "erro": "..." }}. Cenários
 * adaptados da branch feature/crud-completo.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegracaoTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    // ---------- atalhos ----------

    private ResultActions enviar(String metodo, String url, String corpo) throws Exception {
        var requisicao = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> throw new IllegalArgumentException(metodo);
        };
        return mvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String novoCliente() throws Exception {
        String cpf = DadosDeTeste.novoCpf();
        enviar("POST", "/api/clientes", """
            {"nome":"Cliente API","cpf":"%s","email":"api@teste.com","telefone":"11900001111","endereco":"Rua API"}
            """.formatted(cpf)).andExpect(status().isCreated());
        return cpf;
    }

    private String novoProduto(String preco, int estoque) throws Exception {
        String codigo = DadosDeTeste.novoCodigo("API");
        enviar("POST", "/api/produtos", """
            {"codigo":"%s","nome":"Produto API","descricao":"desc","preco":"%s","estoque":%d,"disponivel":true}
            """.formatted(codigo, preco, estoque)).andExpect(status().isCreated());
        return codigo;
    }

    private String novoPedido(String cpf, String codigo, int quantidade) throws Exception {
        String resposta = enviar("POST", "/api/pedidos", """
            {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":%d}]}
            """.formatted(cpf, codigo, quantidade))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("numero").asText();
    }

    private int estoqueDe(String codigo) throws Exception {
        String resposta = mvc.perform(get("/api/produtos/{codigo}", codigo)).andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("quantidadeEmEstoque").asInt();
    }

    // ---------- produtos ----------

    @Nested
    @DisplayName("produtos")
    class Produtos {

        @Test
        @DisplayName("cadastrar, buscar, listar, atualizar e excluir produto nunca vendido")
        void deveFazerCrudCompleto() throws Exception {
            String codigo = novoProduto("19.90", 7);

            mvc.perform(get("/api/produtos/{codigo}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(19.90))
                .andExpect(jsonPath("$.quantidadeEmEstoque").value(7))
                .andExpect(jsonPath("$.ativo").value(true));

            mvc.perform(get("/api/produtos")).andExpect(jsonPath("$[*].codigo", hasItem(codigo)));

            enviar("PUT", "/api/produtos/" + codigo, """
                {"codigo":"%s","nome":"Renomeado","descricao":"","preco":"25.00","estoque":3,"disponivel":false}
                """.formatted(codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Renomeado"))
                .andExpect(jsonPath("$.ativo").value(false));

            mvc.perform(delete("/api/produtos/{codigo}", codigo)).andExpect(status().isNoContent());
            mvc.perform(delete("/api/produtos/{codigo}", codigo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value(containsString("Produto não encontrado")));
        }

        @Test
        @DisplayName("código duplicado → 409; troca de código para um já usado → 409")
        void deveRecusarCodigoDuplicado() throws Exception {
            String codigo = novoProduto("10.00", 1);
            String outro = novoProduto("10.00", 1);

            enviar("POST", "/api/produtos", """
                {"codigo":"%s","nome":"Dup","preco":"1.00","estoque":1}
                """.formatted(codigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("Já existe produto")));

            enviar("PUT", "/api/produtos/" + outro, """
                {"codigo":"%s","nome":"Dup","preco":"1.00","estoque":1}
                """.formatted(codigo))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Bean Validation: campos obrigatórios e preço positivo → 400 com mensagem")
        void deveValidarEntrada() throws Exception {
            enviar("POST", "/api/produtos", """
                {"codigo":"","nome":"X","preco":"0","estoque":-1}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("Preço do produto deve ser maior que zero.")))
                .andExpect(jsonPath("$.erro").value(containsString("Estoque do produto não pode ser negativo.")));
        }

        @Test
        @DisplayName("produto já vendido não pode ser excluído (409)")
        void naoDeveExcluirProdutoVendido() throws Exception {
            String codigo = novoProduto("10.00", 5);
            novoPedido(novoCliente(), codigo, 1);

            mvc.perform(delete("/api/produtos/{codigo}", codigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("Desative-o")));
        }
    }

    // ---------- clientes ----------

    @Nested
    @DisplayName("clientes")
    class Clientes {

        @Test
        @DisplayName("buscar, atualizar e excluir cliente sem pedidos; repetir a exclusão → 404")
        void deveFazerCrudCompleto() throws Exception {
            String cpf = novoCliente();

            mvc.perform(get("/api/clientes/{cpf}", cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.resumo").value("Cliente API (" + cpf + ")"))
                // Campos duplicados removidos do contrato.
                .andExpect(jsonPath("$.documento").doesNotExist())
                .andExpect(jsonPath("$.identificacao").doesNotExist())
                .andExpect(jsonPath("$.endereco.logradouro").value("Rua API"));

            enviar("PUT", "/api/clientes/" + cpf, """
                {"nome":"Nome Novo","cpf":"%s","email":"novo@teste.com","telefone":"11922223333","endereco":""}
                """.formatted(cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.endereco").value(nullValue()));

            mvc.perform(delete("/api/clientes/{cpf}", cpf)).andExpect(status().isNoContent());
            mvc.perform(delete("/api/clientes/{cpf}", cpf)).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CPF duplicado → 409; CPF com dígito errado → 400; atualizar inexistente → 404")
        void deveTratarErros() throws Exception {
            String cpf = novoCliente();
            String corpo = """
                {"nome":"Outro","cpf":"%s","email":"o@teste.com","telefone":"11900000000"}
                """;

            enviar("POST", "/api/clientes", corpo.formatted(cpf)).andExpect(status().isConflict());
            enviar("POST", "/api/clientes", corpo.formatted("12345678901"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("CPF inválido: 12345678901"));
            enviar("PUT", "/api/clientes/" + DadosDeTeste.novoCpf(), corpo.formatted("x"))
                .andExpect(status().isNotFound());
            enviar("POST", "/api/clientes", """
                {"nome":"","cpf":"%s","email":"sem-arroba","telefone":"abc"}
                """.formatted(DadosDeTeste.novoCpf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("E-mail inválido.")));
        }

        @Test
        @DisplayName("cliente com pedidos não pode ser excluído (409)")
        void naoDeveExcluirClienteComPedidos() throws Exception {
            String cpf = novoCliente();
            novoPedido(cpf, novoProduto("10.00", 5), 1);

            mvc.perform(delete("/api/clientes/{cpf}", cpf))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Cliente possui pedidos e não pode ser excluído."));
        }
    }

    // ---------- pedidos e pagamentos ----------

    @Nested
    @DisplayName("pedidos e pagamentos")
    class PedidosEPagamentos {

        @Test
        @DisplayName("estoque insuficiente → 409 e nada é gravado")
        void deveRecusarEstoqueInsuficiente() throws Exception {
            String codigo = novoProduto("10.00", 2);

            enviar("POST", "/api/pedidos", """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":3}]}
                """.formatted(novoCliente(), codigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("Estoque insuficiente")));

            org.junit.jupiter.api.Assertions.assertEquals(2, estoqueDe(codigo));
        }

        @Test
        @DisplayName("cliente/produto/pedido inexistente → 404; sem itens ou quantidade zero → 400")
        void deveTratarEntradasInvalidas() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("10.00", 2);

            enviar("POST", "/api/pedidos", """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":1}]}
                """.formatted(DadosDeTeste.novoCpf(), codigo)).andExpect(status().isNotFound());
            enviar("POST", "/api/pedidos", """
                {"clienteCpf":"%s","itens":[{"codigo":"NAO-EXISTE","quantidade":1}]}
                """.formatted(cpf)).andExpect(status().isNotFound());
            enviar("POST", "/api/pedidos", """
                {"clienteCpf":"%s","itens":[]}
                """.formatted(cpf))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Pedido deve conter pelo menos um item."));
            enviar("POST", "/api/pedidos", """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":0}]}
                """.formatted(cpf, codigo)).andExpect(status().isBadRequest());
            mvc.perform(get("/api/pedidos/{numero}", "PED-0000-00000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Pedido não encontrado: PED-0000-00000"));
        }

        @Test
        @DisplayName("produto desativado não pode ser vendido (409)")
        void naoDeveVenderProdutoInativo() throws Exception {
            String codigo = novoProduto("10.00", 5);
            enviar("PUT", "/api/produtos/" + codigo, """
                {"codigo":"%s","nome":"Inativo","preco":"10.00","estoque":5,"disponivel":false}
                """.formatted(codigo)).andExpect(status().isOk());

            enviar("POST", "/api/pedidos", """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":1}]}
                """.formatted(novoCliente(), codigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("indisponível")));
        }

        @Test
        @DisplayName("pagar com cartão, tentar excluir (409), cancelar e conferir estoque devolvido")
        void deveCumprirCicloDeVida() throws Exception {
            String codigo = novoProduto("50.00", 10);
            String numero = novoPedido(novoCliente(), codigo, 2);

            enviar("POST", "/api/pagamentos", """
                {"tipo":"CARTAO","valor":"100.00","numero":"**** 4242","bandeira":"Visa","parcelas":"3","pedidoNumero":"%s"}
                """.formatted(numero))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").value("Pagamento registrado com sucesso."))
                .andExpect(jsonPath("$.pagamento.descricao").value("CartaoCredito"))
                .andExpect(jsonPath("$.pagamento.quantidadeDeParcelas").value(3));

            mvc.perform(get("/api/pedidos/{numero}", numero))
                .andExpect(jsonPath("$.valorTotal").value(100.00))
                .andExpect(jsonPath("$.itens[0].subtotal").value(100.00))
                .andExpect(jsonPath("$.itens[0].precoPraticado").value(50.00))
                .andExpect(jsonPath("$.situacao").value("PAGO"))
                .andExpect(jsonPath("$.formaPagamento.situacao").value("APROVADO"));

            mvc.perform(get("/api/pagamentos"))
                .andExpect(jsonPath("$[*].metodo", hasItem("Visa •••• **** 4242")));

            mvc.perform(delete("/api/pedidos/{numero}", numero)).andExpect(status().isConflict());

            enviar("PUT", "/api/pedidos/" + numero + "/status", "{\"status\":\"cancelado\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.formaPagamento.situacao").value("ESTORNADO"));
            org.junit.jupiter.api.Assertions.assertEquals(10, estoqueDe(codigo));

            // Pedido cancelado já pode ser excluído.
            mvc.perform(delete("/api/pedidos/{numero}", numero)).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("pagar com boleto e com pix; pagar de novo → 409")
        void devePagarComBoletoEPix() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("30.00", 10);
            String pedidoBoleto = novoPedido(cpf, codigo, 1);
            String pedidoPix = novoPedido(cpf, codigo, 1);

            enviar("POST", "/api/pagamentos", """
                {"tipo":"BOLETO","valor":"30.00","vencimento":"%s","codigo":"34191.79001","pedidoNumero":"%s"}
                """.formatted(LocalDate.now().plusDays(3), pedidoBoleto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pagamento.codigoDeBarras").value("34191.79001"));

            String pix = """
                {"tipo":"PIX","valor":"30.00","chave":"api@teste.com","tipoChave":"EMAIL","pedidoNumero":"%s"}
                """.formatted(pedidoPix);
            enviar("POST", "/api/pagamentos", pix).andExpect(status().isCreated());
            enviar("POST", "/api/pagamentos", pix)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("não pode ser pago")));
        }

        @Test
        @DisplayName("valor diferente do total → 409; tipo inválido e campos faltando → 400")
        void deveValidarPagamento() throws Exception {
            String numero = novoPedido(novoCliente(), novoProduto("30.00", 10), 1);

            enviar("POST", "/api/pagamentos", """
                {"tipo":"PIX","valor":"29.00","chave":"c","tipoChave":"EMAIL","pedidoNumero":"%s"}
                """.formatted(numero))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Valor do pagamento deve ser igual ao total do pedido"));
            enviar("POST", "/api/pagamentos", """
                {"tipo":"CHEQUE","valor":"30.00","pedidoNumero":"%s"}
                """.formatted(numero)).andExpect(status().isBadRequest());
            enviar("POST", "/api/pagamentos", """
                {"tipo":"PIX","valor":"30.00","pedidoNumero":"%s"}
                """.formatted(numero))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Chave Pix é obrigatório."));
            enviar("POST", "/api/pagamentos", "{\"tipo\":\"PIX\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("Número do pedido é obrigatório")));
        }

        @Test
        @DisplayName("excluir pedido em aberto devolve o estoque; status inválido → 400; reabrir pago → 409")
        void deveExcluirPedidoAbertoEValidarStatus() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("10.00", 5);
            String numero = novoPedido(cpf, codigo, 3);
            org.junit.jupiter.api.Assertions.assertEquals(2, estoqueDe(codigo));

            enviar("PUT", "/api/pedidos/" + numero + "/status", "{\"status\":\"ENVIADO\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("Status inválido")));
            enviar("PUT", "/api/pedidos/" + numero + "/status", "{\"status\":\"PAGO\"}")
                .andExpect(status().isConflict());
            enviar("PUT", "/api/pedidos/" + numero + "/status", "{\"status\":\"ABERTO\"}")
                .andExpect(status().isOk());

            mvc.perform(delete("/api/pedidos/{numero}", numero)).andExpect(status().isNoContent());
            org.junit.jupiter.api.Assertions.assertEquals(5, estoqueDe(codigo));
            mvc.perform(get("/api/pedidos/{numero}", numero)).andExpect(status().isNotFound());

            String pago = novoPedido(cpf, codigo, 1);
            enviar("POST", "/api/pagamentos", """
                {"tipo":"PIX","valor":"10.00","chave":"c","tipoChave":"EMAIL","pedidoNumero":"%s"}
                """.formatted(pago)).andExpect(status().isCreated());
            enviar("PUT", "/api/pedidos/" + pago + "/status", "{\"status\":\"ABERTO\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Transição de situação inválida"));
        }

        @Test
        @DisplayName("dashboard e histórico refletem os dados gravados")
        void deveMostrarDashboardEHistorico() throws Exception {
            String numero = novoPedido(novoCliente(), novoProduto("12.50", 3), 2);

            String corpo = mvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.estoqueCritico").isArray())
                .andExpect(jsonPath("$.pedidosRecentes[0].numero").exists())
                .andReturn().getResponse().getContentAsString();
            JsonNode dashboard = json.readTree(corpo);
            org.junit.jupiter.api.Assertions.assertTrue(dashboard.get("receitaTotal").decimalValue().signum() > 0);

            mvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItem("Pedido " + numero + " criado")));
            mvc.perform(get("/api/pedidos")).andExpect(jsonPath("$[*].numero", hasItem(numero)));
        }
    }

    // ---------- erros genéricos ----------

    @Test
    @DisplayName("JSON malformado → 400 e rota inexistente → 404, sempre com a chave 'erro'")
    void deveTratarErrosDoSpring() throws Exception {
        enviar("POST", "/api/clientes", "{ isto não é json")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido ou mal formatado."));
        mvc.perform(get("/api/nao-existe"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.erro").value(containsString("Rota não encontrada")));
        mvc.perform(delete("/api/dashboard"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.erro").exists());
    }
}
