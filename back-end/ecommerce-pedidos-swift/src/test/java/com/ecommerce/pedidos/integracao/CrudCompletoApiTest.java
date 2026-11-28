package com.ecommerce.pedidos.integracao;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * CRUD completo da API (soft delete, reativação, filtros, paginação, estoque
 * parcial, versão otimista, cancelamento e remoção de item), de ponta a ponta
 * contra o H2.
 *
 * <p>Cenários portados do ApiIntegracaoTest da branch feature/crud-completo,
 * que rodava contra um MySQL limpo a cada teste. Aqui o H2 é compartilhado
 * entre as classes, então cada teste cria dados únicos e confere a presença
 * ou ausência DELES (com o filtro {@code nome}) em vez do tamanho total da lista.</p>
 */
@Tag("integracao")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrudCompletoApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, String corpo) throws Exception {
        return mvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String novoCliente() throws Exception {
        String cpf = DadosDeTeste.novoCpf();
        enviar(post("/api/clientes"), """
            {"nome":"Lia Souza","cpf":"%1$s","email":"lia%1$s@email.com","telefone":"11999998888","endereco":"Rua A, 1"}
            """.formatted(cpf)).andExpect(status().isCreated());
        return cpf;
    }

    /** O nome leva o código, para a busca por nome isolar os produtos de cada teste. */
    private String novoProduto(String prefixo, String preco, int estoque) throws Exception {
        String codigo = DadosDeTeste.novoCodigo(prefixo);
        enviar(post("/api/produtos"), """
            {"codigo":"%1$s","nome":"Produto %1$s","descricao":"","preco":"%2$s","estoque":%3$d,"disponivel":true}
            """.formatted(codigo, preco, estoque)).andExpect(status().isCreated());
        return codigo;
    }

    private JsonNode criarPedido(String cpf, String itensJson) throws Exception {
        String resposta = enviar(post("/api/pedidos"), """
            {"clienteCpf":"%s","itens":%s}
            """.formatted(cpf, itensJson))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta);
    }

    private String criarPedido(String cpf, String codigo, int quantidade) throws Exception {
        return criarPedido(cpf, "[{\"codigo\":\"%s\",\"quantidade\":%d}]".formatted(codigo, quantidade))
            .get("numero").asText();
    }

    private int estoqueDe(String codigo) throws Exception {
        String resposta = mvc.perform(get("/api/produtos/{codigo}", codigo)).andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("quantidadeEmEstoque").asInt();
    }

    @Nested
    @DisplayName("produtos")
    class Produtos {

        @Test
        @DisplayName("cadastrar, buscar, atualizar (versão sobe), baixar estoque parcial e conferir")
        void cicloDeAtualizacao() throws Exception {
            String codigo = novoProduto("P-CIC", "50.00", 5);

            mvc.perform(get("/api/produtos/{codigo}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto " + codigo))
                .andExpect(jsonPath("$.versao").value(0));

            enviar(put("/api/produtos/" + codigo), """
                {"codigo":"%s","nome":"Camiseta","descricao":"Algodão","preco":"59.90","estoque":7,"disponivel":true}
                """.formatted(codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1));

            enviar(patch("/api/produtos/" + codigo + "/estoque"), "{\"quantidade\":12}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEmEstoque").value(12))
                .andExpect(jsonPath("$.nome").value("Camiseta"))
                .andExpect(jsonPath("$.preco").value(59.90));
        }

        @Test
        @DisplayName("PUT sem estoque mantém o estoque atual")
        void putSemEstoque() throws Exception {
            String codigo = novoProduto("P-SEM", "10.00", 4);
            enviar(put("/api/produtos/" + codigo), """
                {"codigo":"%s","nome":"Novo nome","preco":"11.00"}
                """.formatted(codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEmEstoque").value(4));
        }

        @Test
        @DisplayName("versão desatualizada no PUT devolve 409")
        void versaoDesatualizada() throws Exception {
            String codigo = novoProduto("P-VER", "10.00", 1);
            enviar(put("/api/produtos/" + codigo), """
                {"codigo":"%s","nome":"A","preco":"11.00","estoque":1,"versao":0}
                """.formatted(codigo)).andExpect(status().isOk());

            enviar(put("/api/produtos/" + codigo), """
                {"codigo":"%s","nome":"B","preco":"12.00","estoque":1,"versao":0}
                """.formatted(codigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("alterado(a) por outra operação")));
        }

        @Test
        @DisplayName("DELETE inativa (soft delete); inativos só aparecem quando pedidos; reativar devolve ao catálogo")
        void softDelete() throws Exception {
            String ativo = novoProduto("P-ATV", "10.00", 1);
            String inativo = novoProduto("P-INA", "10.00", 1);

            mvc.perform(delete("/api/produtos/{codigo}", inativo)).andExpect(status().isNoContent());

            mvc.perform(get("/api/produtos"))
                .andExpect(jsonPath("$[*].codigo", hasItem(ativo)))
                .andExpect(jsonPath("$[*].codigo", not(hasItem(inativo))));
            mvc.perform(get("/api/produtos").param("situacao", "inativos"))
                .andExpect(jsonPath("$[*].codigo", hasItem(inativo)))
                .andExpect(jsonPath("$[*].ativo", everyItem(org.hamcrest.Matchers.is(false))));
            mvc.perform(get("/api/produtos").param("situacao", "todos"))
                .andExpect(jsonPath("$[*].codigo", hasItem(ativo)))
                .andExpect(jsonPath("$[*].codigo", hasItem(inativo)));
            mvc.perform(get("/api/produtos/{codigo}", inativo)).andExpect(status().isNotFound());
            mvc.perform(delete("/api/produtos/{codigo}", inativo)).andExpect(status().isNotFound());

            enviar(patch("/api/produtos/{codigo}", inativo), "{\"ativo\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
            enviar(patch("/api/produtos/{codigo}", inativo), "{\"ativo\":true}").andExpect(status().isNotFound());
            enviar(patch("/api/produtos/{codigo}", inativo), "{\"ativo\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        }

        @Test
        @DisplayName("exclusão definitiva: 204 se nunca vendido, 409 se já vendido")
        void exclusaoDefinitiva() throws Exception {
            String cpf = novoCliente();
            String novo = novoProduto("P-NOVO", "10.00", 1);
            String vendido = novoProduto("P-VEND", "10.00", 5);
            criarPedido(cpf, vendido, 1);

            mvc.perform(delete("/api/produtos/{codigo}", novo).param("definitivo", "true"))
                .andExpect(status().isNoContent());
            mvc.perform(get("/api/produtos").param("situacao", "todos"))
                .andExpect(jsonPath("$[*].codigo", not(hasItem(novo))));

            mvc.perform(delete("/api/produtos/{codigo}", vendido).param("definitivo", "true"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(startsWith("Este produto já foi vendido")));
        }

        @Test
        @DisplayName("busca por nome, paginação e erros de entrada")
        void buscaPaginacaoEErros() throws Exception {
            String grupo = DadosDeTeste.novoCodigo("PAG");
            String a = novoProduto(grupo + "-A", "10.00", 1);
            novoProduto(grupo + "-B", "10.00", 1);
            String c = novoProduto(grupo + "-C", "10.00", 1);

            mvc.perform(get("/api/produtos").param("nome", a)).andExpect(jsonPath("$", hasSize(1)));
            // Ordem por nome: A, B, C. Página 1 com 2 itens = só o C.
            mvc.perform(get("/api/produtos").param("nome", grupo).param("pagina", "1").param("tamanho", "2"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value(c));
            mvc.perform(get("/api/produtos").param("situacao", "qualquer")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/produtos").param("tamanho", "9999")).andExpect(status().isBadRequest());
            mvc.perform(get("/api/produtos").param("pagina", "-1")).andExpect(status().isBadRequest());
            enviar(post("/api/produtos"), "{\"codigo\":\"%s\",\"nome\":\"Dup\",\"preco\":\"1.00\",\"estoque\":1}".formatted(a))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um produto com este código."));
            enviar(post("/api/produtos"), "{\"codigo\":\"P-X1\",\"nome\":\"Sem preço\",\"estoque\":1}")
                .andExpect(status().isBadRequest());
            enviar(post("/api/produtos"), "{\"codigo\":\"P-X2\",\"nome\":\"Sem estoque\",\"preco\":\"1.00\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Estoque do produto é obrigatório."));
            enviar(patch("/api/produtos/" + a + "/estoque"), "{}").andExpect(status().isBadRequest());
            enviar(patch("/api/produtos/" + a + "/estoque"), "{\"quantidade\":-1}").andExpect(status().isBadRequest());
            enviar(put("/api/produtos/NAO-EXISTE"), "{\"codigo\":\"NAO-EXISTE\",\"nome\":\"X\",\"preco\":\"1.00\"}")
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("clientes")
    class Clientes {

        @Test
        @DisplayName("buscar e atualizar sem CPF no corpo; atualizar inexistente devolve 404")
        void buscarEAtualizar() throws Exception {
            String cpf = novoCliente();
            mvc.perform(get("/api/clientes/{cpf}", cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lia" + cpf + "@email.com"))
                .andExpect(jsonPath("$.ativo").value(true));

            enviar(put("/api/clientes/" + cpf), """
                {"nome":"Lia S. Souza","email":"lia.souza%s@email.com","telefone":"11911112222","endereco":"Rua B, 2"}
                """.formatted(cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lia S. Souza"));
            mvc.perform(get("/api/clientes/{cpf}", cpf)).andExpect(jsonPath("$.email").value("lia.souza" + cpf + "@email.com"));

            String inexistente = DadosDeTeste.novoCpf();
            enviar(put("/api/clientes/" + inexistente), """
                {"nome":"X","email":"x@email.com","telefone":"11999999","endereco":"r"}
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(containsString(inexistente)));
        }

        @Test
        @DisplayName("e-mail já usado por outro cliente devolve 409 no cadastro e na atualização")
        void emailDuplicado() throws Exception {
            String primeiro = novoCliente();
            String segundo = novoCliente();
            String emailDoPrimeiro = "lia" + primeiro + "@email.com";

            enviar(post("/api/clientes"), """
                {"nome":"Outro","cpf":"%s","email":"%s","telefone":"11900000000"}
                """.formatted(DadosDeTeste.novoCpf(), emailDoPrimeiro))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("e-mail")));
            enviar(put("/api/clientes/" + segundo), """
                {"nome":"Lia","email":"%s","telefone":"11900000000"}
                """.formatted(emailDoPrimeiro))
                .andExpect(status().isConflict());
            // O próprio e-mail pode ser reenviado na atualização.
            enviar(put("/api/clientes/" + primeiro), """
                {"nome":"Lia","email":"%s","telefone":"11900000000"}
                """.formatted(emailDoPrimeiro))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("cliente com pedidos: DELETE 409; inativar esconde da lista mas mantém o histórico")
        void restricaoESoftDelete() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("P-CLI", "25.00", 3);
            criarPedido(cpf, codigo, 1);

            mvc.perform(delete("/api/clientes/{cpf}", cpf))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(startsWith("Este cliente possui pedidos e não pode ser excluído.")));

            enviar(patch("/api/clientes/{cpf}", cpf), "{\"ativo\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
            mvc.perform(get("/api/clientes")).andExpect(jsonPath("$[*].cpf", not(hasItem(cpf))));
            mvc.perform(get("/api/clientes").param("situacao", "inativos")).andExpect(jsonPath("$[*].cpf", hasItem(cpf)));
            mvc.perform(get("/api/clientes/{cpf}", cpf)).andExpect(status().isNotFound());
            mvc.perform(get("/api/clientes/{cpf}/pedidos", cpf)).andExpect(jsonPath("$", hasSize(1)));
            // Inativo não compra.
            enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":1}]}
                """.formatted(cpf, codigo)).andExpect(status().isNotFound());

            enviar(patch("/api/clientes/{cpf}", cpf), "{\"ativo\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
            mvc.perform(get("/api/clientes/{cpf}/pedidos", DadosDeTeste.novoCpf())).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("cliente sem pedidos é excluído; repetir a exclusão devolve 404; CPF duplicado devolve 409")
        void exclusao() throws Exception {
            String cpf = novoCliente();
            enviar(post("/api/clientes"), """
                {"nome":"Outro","cpf":"%s","email":"outro%s@email.com","telefone":"11900000000","endereco":"Rua C"}
                """.formatted(cpf, cpf)).andExpect(status().isConflict());

            mvc.perform(delete("/api/clientes/{cpf}", cpf)).andExpect(status().isNoContent());
            mvc.perform(delete("/api/clientes/{cpf}", cpf)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("pedidos e pagamentos")
    class Pedidos {

        @Test
        @DisplayName("estoque insuficiente devolve 409 e não grava nada")
        void estoqueInsuficiente() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("P-PED", "50.00", 5);

            enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":9}]}
                """.formatted(cpf, codigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Estoque insuficiente de Produto " + codigo + ": disponível 5, solicitado 9"));

            mvc.perform(get("/api/clientes/{cpf}/pedidos", cpf)).andExpect(jsonPath("$", hasSize(0)));
            assertEquals(5, estoqueDe(codigo));
        }

        @Test
        @DisplayName("produto ou cliente inexistente devolve 404; pedido sem itens devolve 400")
        void referenciasInvalidas() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("P-REF", "50.00", 5);
            enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"NAO-EXISTE","quantidade":1}]}
                """.formatted(cpf)).andExpect(status().isNotFound());
            enviar(post("/api/pedidos"), """
                {"clienteCpf":"%s","itens":[{"codigo":"%s","quantidade":1}]}
                """.formatted(DadosDeTeste.novoCpf(), codigo)).andExpect(status().isNotFound());
            enviar(post("/api/pedidos"), "{\"clienteCpf\":\"%s\",\"itens\":[]}".formatted(cpf))
                .andExpect(status().isBadRequest());
            mvc.perform(get("/api/pedidos/NAO-EXISTE")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("pagar, tentar excluir (409), cancelar e conferir estoque devolvido")
        void pagarECancelar() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("P-PAG", "50.00", 5);
            String numero = criarPedido(cpf, codigo, 2);
            assertEquals(3, estoqueDe(codigo));

            enviar(patch("/api/pedidos/{numero}", numero), "{\"situacao\":\"ABERTO\"}").andExpect(status().isOk());
            enviar(patch("/api/pedidos/{numero}", numero), "{\"situacao\":\"PAGO\"}").andExpect(status().isConflict());

            enviar(post("/api/pedidos/{numero}/pagamento", numero), """
                {"tipo":"PIX","valor":"100.00","chave":"lia@email.com","tipoChave":"EMAIL"}
                """).andExpect(status().isCreated());
            mvc.perform(get("/api/pedidos").param("situacao", "pago"))
                .andExpect(jsonPath("$[*].numero", hasItem(numero)))
                .andExpect(jsonPath("$[*].situacao", everyItem(org.hamcrest.Matchers.is("PAGO"))));
            mvc.perform(get("/api/pedidos").param("situacao", "aberto"))
                .andExpect(jsonPath("$[*].numero", not(hasItem(numero))));
            mvc.perform(get("/api/pedidos").param("situacao", "qualquer")).andExpect(status().isBadRequest());

            mvc.perform(delete("/api/pedidos/{numero}", numero))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("Cancele o pedido")));

            enviar(patch("/api/pedidos/{numero}", numero), "{\"situacao\":\"CANCELADO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.formaPagamento.situacao").value("ESTORNADO"));
            assertEquals(5, estoqueDe(codigo));
            enviar(patch("/api/pedidos/{numero}", numero), "{\"situacao\":\"CANCELADO\"}").andExpect(status().isConflict());
        }

        @Test
        @DisplayName("remover item e excluir pedido em aberto (cascade) devolvem o estoque")
        void removerItemEExcluir() throws Exception {
            String cpf = novoCliente();
            String principal = novoProduto("P-PRI", "50.00", 5);
            String extra = novoProduto("P-EXT", "10.00", 4);
            JsonNode pedido = criarPedido(cpf, """
                [{"codigo":"%s","quantidade":1},{"codigo":"%s","quantidade":2}]
                """.formatted(principal, extra));
            String numero = pedido.get("numero").asText();
            long itemExtra = -1;
            for (JsonNode item : pedido.get("itens")) {
                if (item.get("produto").get("codigo").asText().equals(extra)) {
                    itemExtra = item.get("id").asLong();
                }
            }

            mvc.perform(delete("/api/pedidos/{numero}/itens/{id}", numero, itemExtra)).andExpect(status().isNoContent());
            mvc.perform(get("/api/pedidos/{numero}", numero)).andExpect(jsonPath("$.itens", hasSize(1)));
            assertEquals(4, estoqueDe(extra));
            mvc.perform(delete("/api/pedidos/{numero}/itens/{id}", numero, 999_999)).andExpect(status().isNotFound());

            mvc.perform(delete("/api/pedidos/{numero}", numero)).andExpect(status().isNoContent());
            mvc.perform(get("/api/pedidos/{numero}", numero)).andExpect(status().isNotFound());
            assertEquals(5, estoqueDe(principal));
        }

        @Test
        @DisplayName("pedido que não está aberto não tem itens removidos")
        void removerItemDePedidoCancelado() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("P-CAN", "10.00", 3);
            JsonNode pedido = criarPedido(cpf, "[{\"codigo\":\"%s\",\"quantidade\":1}]".formatted(codigo));
            String numero = pedido.get("numero").asText();
            long item = pedido.get("itens").get(0).get("id").asLong();

            enviar(patch("/api/pedidos/{numero}", numero), "{\"situacao\":\"CANCELADO\"}").andExpect(status().isOk());
            mvc.perform(delete("/api/pedidos/{numero}/itens/{id}", numero, item)).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("dashboard e histórico refletem os dados gravados; listagem de pedidos pagina")
        void dashboardEHistorico() throws Exception {
            String cpf = novoCliente();
            String codigo = novoProduto("P-DSH", "50.00", 5);
            String numero = criarPedido(cpf, codigo, 2);

            mvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueCritico[*].codigo", hasItem(codigo)));
            mvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItem("Pedido " + numero + " criado")));
            mvc.perform(get("/api/pedidos").param("pagina", "0").param("tamanho", "1"))
                .andExpect(jsonPath("$", hasSize(1)));
            mvc.perform(get("/api/clientes/{cpf}/pedidos", cpf).param("tamanho", "1"))
                .andExpect(jsonPath("$[0].numero").value(numero));
        }
    }
}
