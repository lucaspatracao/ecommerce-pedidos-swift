package com.ecommerce.pedidos.integracao;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Integração ponta a ponta: requisição HTTP → controller → serviço →
 * repositório → MySQL (banco {@code ecommerce_test}). Sobe o contexto Spring
 * completo, inclusive a aplicação automática das migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegracaoTest extends IntegracaoBancoBase {
    private static final String CPF = "12345678900";

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void apontarAplicacaoParaBancoDeTeste() {
        // ConexaoFactory lê DB_URL a cada conexão: a aplicação usa o banco de teste.
        System.setProperty("DB_URL", urlTeste());
    }

    @AfterAll
    static void restaurarConfiguracao() {
        System.clearProperty("DB_URL");
    }

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
            String json) throws Exception {
        return mvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void cadastrarCliente() throws Exception {
        enviar(post("/api/clientes"), """
            {"nome":"Lia Souza","cpf":"%s","email":"lia@email.com","telefone":"11999998888","endereco":"Rua A, 1"}
            """.formatted(CPF)).andExpect(status().isCreated());
    }

    private void cadastrarProduto(String codigo, String preco, int estoque) throws Exception {
        enviar(post("/api/produtos"), """
            {"codigo":"%s","nome":"Produto %s","descricao":"","preco":"%s","estoque":%d,"disponivel":true}
            """.formatted(codigo, codigo, preco, estoque)).andExpect(status().isCreated());
    }

    private String criarPedido(String codigo, int quantidade) throws Exception {
        String resposta = enviar(post("/api/pedidos"), """
            {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":%d}]}
            """.formatted(CPF, codigo, quantidade))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.numero");
    }

    @Nested
    @DisplayName("produtos")
    class Produtos {

        @Test
        @DisplayName("cadastrar, buscar, atualizar, baixar estoque parcial e conferir")
        void cicloDeAtualizacao() throws Exception {
            cadastrarProduto("P-001", "50.00", 5);

            mvc.perform(get("/api/produtos/P-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto P-001"))
                .andExpect(jsonPath("$.versao").value(0));

            enviar(put("/api/produtos/P-001"), """
                {"codigo":"P-001","nome":"Camiseta","descricao":"Algodão","preco":"59.90","estoque":7,"disponivel":true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1));

            enviar(patch("/api/produtos/P-001/estoque"), "{\"quantidade\":12}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEmEstoque").value(12))
                .andExpect(jsonPath("$.nome").value("Camiseta"))
                .andExpect(jsonPath("$.preco").value(59.90));
        }

        @Test
        @DisplayName("versão desatualizada no PUT devolve 409")
        void versaoDesatualizada() throws Exception {
            cadastrarProduto("P-VER", "10.00", 1);
            enviar(put("/api/produtos/P-VER"), """
                {"codigo":"P-VER","nome":"A","preco":"11.00","estoque":1,"versao":0}
                """).andExpect(status().isOk());

            enviar(put("/api/produtos/P-VER"), """
                {"codigo":"P-VER","nome":"B","preco":"12.00","estoque":1,"versao":0}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("alterado(a) por outra operação")));
        }

        @Test
        @DisplayName("DELETE inativa (soft delete); inativos só aparecem quando pedidos; reativar devolve ao catálogo")
        void softDelete() throws Exception {
            cadastrarProduto("P-ATV", "10.00", 1);
            cadastrarProduto("P-INA", "10.00", 1);

            mvc.perform(delete("/api/produtos/P-INA")).andExpect(status().isNoContent());

            mvc.perform(get("/api/produtos"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value("P-ATV"));
            mvc.perform(get("/api/produtos").param("situacao", "inativos"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ativo").value(false));
            mvc.perform(get("/api/produtos").param("situacao", "todos")).andExpect(jsonPath("$", hasSize(2)));
            mvc.perform(get("/api/produtos/P-INA")).andExpect(status().isNotFound());

            mvc.perform(post("/api/produtos/P-INA/reativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
            mvc.perform(post("/api/produtos/P-INA/inativar")).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("exclusão definitiva: 204 se nunca vendido, 409 se já vendido")
        void exclusaoDefinitiva() throws Exception {
            cadastrarCliente();
            cadastrarProduto("P-NOVO", "10.00", 1);
            cadastrarProduto("P-VEND", "10.00", 5);
            criarPedido("P-VEND", 1);

            mvc.perform(delete("/api/produtos/P-NOVO").param("definitivo", "true")).andExpect(status().isNoContent());
            mvc.perform(get("/api/produtos").param("situacao", "todos"))
                .andExpect(jsonPath("$", hasSize(1)));

            mvc.perform(delete("/api/produtos/P-VEND").param("definitivo", "true"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(startsWith("Este produto já foi vendido")));
        }

        @Test
        @DisplayName("busca por nome, paginação e erros de entrada")
        void buscaPaginacaoEErros() throws Exception {
            cadastrarProduto("P-A", "10.00", 1);
            cadastrarProduto("P-B", "10.00", 1);
            cadastrarProduto("P-C", "10.00", 1);

            mvc.perform(get("/api/produtos").param("nome", "P-B")).andExpect(jsonPath("$", hasSize(1)));
            mvc.perform(get("/api/produtos").param("pagina", "1").param("tamanho", "2"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value("P-C"));
            mvc.perform(get("/api/produtos").param("situacao", "qualquer")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/produtos").param("tamanho", "9999")).andExpect(status().isBadRequest());
            enviar(post("/api/produtos"), "{\"codigo\":\"P-A\",\"nome\":\"Dup\",\"preco\":\"1.00\",\"estoque\":1}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Já existe um produto com este código."));
            enviar(post("/api/produtos"), "{\"codigo\":\"P-X\",\"nome\":\"Sem preço\",\"estoque\":1}")
                .andExpect(status().isBadRequest());
            enviar(patch("/api/produtos/P-A/estoque"), "{}").andExpect(status().isBadRequest());
            enviar(put("/api/produtos/NAO-EXISTE"), "{\"codigo\":\"NAO-EXISTE\",\"nome\":\"X\",\"preco\":\"1.00\"}")
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("clientes")
    class Clientes {

        @BeforeEach
        void preparar() throws Exception {
            cadastrarCliente();
        }

        @Test
        @DisplayName("buscar e atualizar; atualizar inexistente devolve 404")
        void buscarEAtualizar() throws Exception {
            mvc.perform(get("/api/clientes/" + CPF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lia@email.com"));

            enviar(put("/api/clientes/" + CPF), """
                {"nome":"Lia S. Souza","email":"lia.souza@email.com","telefone":"11911112222","endereco":"Rua B, 2"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lia S. Souza"));
            mvc.perform(get("/api/clientes/" + CPF)).andExpect(jsonPath("$.email").value("lia.souza@email.com"));

            enviar(put("/api/clientes/00000000000"), """
                {"nome":"X","email":"x@email.com","telefone":"1","endereco":"r"}
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value(containsString("00000000000")));
        }

        @Test
        @DisplayName("cliente com pedidos: DELETE 409; inativar esconde da lista mas mantém o histórico")
        void restricaoESoftDelete() throws Exception {
            cadastrarProduto("P-CLI", "25.00", 3);
            criarPedido("P-CLI", 1);

            mvc.perform(delete("/api/clientes/" + CPF))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(startsWith("Este cliente possui pedidos e não pode ser excluído.")));

            mvc.perform(post("/api/clientes/" + CPF + "/inativar")).andExpect(status().isNoContent());
            mvc.perform(get("/api/clientes")).andExpect(jsonPath("$", empty()));
            mvc.perform(get("/api/clientes").param("situacao", "inativos")).andExpect(jsonPath("$", hasSize(1)));
            mvc.perform(get("/api/clientes/" + CPF + "/pedidos")).andExpect(jsonPath("$", hasSize(1)));

            mvc.perform(post("/api/clientes/" + CPF + "/reativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("cliente sem pedidos é excluído; repetir a exclusão devolve 404; CPF duplicado devolve 409")
        void exclusao() throws Exception {
            enviar(post("/api/clientes"), """
                {"nome":"Outro","cpf":"%s","email":"outro@email.com","telefone":"11900000000","endereco":"Rua C"}
                """.formatted(CPF)).andExpect(status().isConflict());

            mvc.perform(delete("/api/clientes/" + CPF)).andExpect(status().isNoContent());
            mvc.perform(delete("/api/clientes/" + CPF)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("pedidos e pagamentos")
    class Pedidos {

        @BeforeEach
        void preparar() throws Exception {
            cadastrarCliente();
            cadastrarProduto("P-PED", "50.00", 5);
        }

        @Test
        @DisplayName("estoque insuficiente devolve 422 e não grava nada")
        void estoqueInsuficiente() throws Exception {
            enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"P-PED","quantidade":9}]}
                """.formatted(CPF))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("Estoque insuficiente de Produto P-PED: disponível 5, solicitado 9"));

            mvc.perform(get("/api/pedidos")).andExpect(jsonPath("$", empty()));
            mvc.perform(get("/api/produtos/P-PED")).andExpect(jsonPath("$.quantidadeEmEstoque").value(5));
        }

        @Test
        @DisplayName("produto ou cliente inexistente devolve 404; pedido sem itens devolve 400")
        void referenciasInvalidas() throws Exception {
            enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"NAO-EXISTE","quantidade":1}]}
                """.formatted(CPF)).andExpect(status().isNotFound());
            enviar(post("/api/pedidos"), """
                {"clienteCpf":"99999999999","itens":[{"codigo":"P-PED","quantidade":1}]}
                """).andExpect(status().isNotFound());
            enviar(post("/api/pedidos"), "{\"clienteCpf\":\"%s\",\"itens\":[]}".formatted(CPF))
                .andExpect(status().isBadRequest());
            mvc.perform(get("/api/pedidos/NAO-EXISTE")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("pagar, tentar excluir (409), cancelar e conferir estoque devolvido")
        void pagarECancelar() throws Exception {
            String numero = criarPedido("P-PED", 2);
            mvc.perform(get("/api/produtos/P-PED")).andExpect(jsonPath("$.quantidadeEmEstoque").value(3));

            enviar(put("/api/pedidos/" + numero + "/status"), "{\"status\":\"ABERTO\"}").andExpect(status().isOk());
            enviar(put("/api/pedidos/" + numero + "/status"), "{\"status\":\"PAGO\"}").andExpect(status().isConflict());

            enviar(post("/api/pagamentos"), """
                {"tipo":"PIX","valor":"100.00","chave":"lia@email.com","tipoChave":"EMAIL","pedidoNumero":"%s"}
                """.formatted(numero)).andExpect(status().isCreated());
            mvc.perform(get("/api/pagamentos")).andExpect(jsonPath("$[0].tipo").value("Pix"));
            mvc.perform(get("/api/pedidos").param("situacao", "pago")).andExpect(jsonPath("$", hasSize(1)));

            mvc.perform(delete("/api/pedidos/" + numero))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("Cancele o pedido")));

            mvc.perform(post("/api/pedidos/" + numero + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADO"));
            mvc.perform(get("/api/produtos/P-PED")).andExpect(jsonPath("$.quantidadeEmEstoque").value(5));
            mvc.perform(post("/api/pedidos/" + numero + "/cancelar")).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("valor de pagamento diferente do total devolve 409; tipo inválido devolve 400")
        void pagamentoInvalido() throws Exception {
            String numero = criarPedido("P-PED", 1);

            enviar(post("/api/pagamentos"), """
                {"tipo":"PIX","valor":"1.00","chave":"lia@email.com","tipoChave":"EMAIL","pedidoNumero":"%s"}
                """.formatted(numero)).andExpect(status().isConflict());
            enviar(post("/api/pagamentos"), """
                {"tipo":"CHEQUE","valor":"50.00","pedidoNumero":"%s"}
                """.formatted(numero)).andExpect(status().isBadRequest());
            enviar(post("/api/pagamentos"), "{\"tipo\":\"PIX\"}").andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("remover item e excluir pedido em aberto (cascade) devolvem o estoque")
        void removerItemEExcluir() throws Exception {
            cadastrarProduto("P-EXT", "10.00", 4);
            String resposta = enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"P-PED","quantidade":1},{"codigo":"P-EXT","quantidade":2}]}
                """.formatted(CPF)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            String numero = JsonPath.read(resposta, "$.numero");
            java.util.List<Integer> idsExtra = JsonPath.read(resposta, "$.itens[?(@.produto.codigo == 'P-EXT')].id");
            Integer itemExtra = idsExtra.get(0);

            mvc.perform(delete("/api/pedidos/" + numero + "/itens/" + itemExtra))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)));
            mvc.perform(get("/api/produtos/P-EXT")).andExpect(jsonPath("$.quantidadeEmEstoque").value(4));
            mvc.perform(delete("/api/pedidos/" + numero + "/itens/999999")).andExpect(status().isNotFound());

            mvc.perform(delete("/api/pedidos/" + numero)).andExpect(status().isNoContent());
            mvc.perform(get("/api/pedidos/" + numero)).andExpect(status().isNotFound());
            mvc.perform(get("/api/produtos/P-PED")).andExpect(jsonPath("$.quantidadeEmEstoque").value(5));
        }

        @Test
        @DisplayName("dashboard e histórico refletem os dados gravados")
        void dashboardEHistorico() throws Exception {
            String numero = criarPedido("P-PED", 2);

            mvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientes").value(1))
                .andExpect(jsonPath("$.totalProdutos").value(1))
                .andExpect(jsonPath("$.totalPedidos").value(1))
                .andExpect(jsonPath("$.receitaTotal").value(100.00))
                .andExpect(jsonPath("$.estoqueCritico", hasSize(1)));
            mvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Pedido " + numero + " criado"));
            mvc.perform(get("/api/pedidos").param("pagina", "0").param("tamanho", "1"))
                .andExpect(jsonPath("$", hasSize(1)));
        }
    }
}
