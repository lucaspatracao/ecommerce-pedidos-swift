package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.mysql.ItemPedidoRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.ProdutoRepositoryMySql;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.PedidoService.ItemPedidoRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os quatro cenários da finalização transacional do pedido. Nos cenários de
 * falha, o teste confirma que NADA ficou gravado: nenhum pedido, nenhum item
 * e nenhuma alteração de estoque.
 */
class FinalizacaoPedidoTransacaoTest extends IntegracaoBancoBase {
    private static final String CPF = "77788899900";
    private static final List<ItemPedidoRequest> ITENS = List.of(
        new ItemPedidoRequest("PRD-T1", 2),
        new ItemPedidoRequest("PRD-T2", 3));

    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void preparar() {
        criarCliente(CPF, "heitor@email.com");
        produto1 = criarProduto("PRD-T1", "10.00", 5);
        produto2 = criarProduto("PRD-T2", "20.00", 5);
    }

    private void assertNadaGravado() throws SQLException {
        assertEquals(0, contar("pedido"), "nenhum pedido deve permanecer");
        assertEquals(0, contar("item_pedido"), "nenhum item deve permanecer");
        assertEquals(5, estoqueNoBanco(produto1.getId()), "estoque do produto 1 deve voltar ao original");
    }

    @Test
    @DisplayName("1) sucesso completo: pedido, itens e baixa de estoque gravados juntos")
    void sucessoCompleto() throws Exception {
        Pedido pedido = pedidoService().criarPedido(CPF, ITENS);

        assertNotNull(pedido.getId());
        assertTrue(pedido.getItens().stream().allMatch(item -> item.getId() != null));
        assertEquals(1, contar("pedido"));
        assertEquals(2, contar("item_pedido"));
        assertEquals(3, estoqueNoBanco(produto1.getId()));
        assertEquals(2, estoqueNoBanco(produto2.getId()));
    }

    @Test
    @DisplayName("2) falha ao gravar o segundo item: rollback completo")
    void falhaAoGravarSegundoItem() throws Exception {
        ItemPedidoRepositoryMySql itensQueFalham = new ItemPedidoRepositoryMySql() {
            private int chamadas;

            @Override
            public ItemPedido inserir(Connection conexao, Long pedidoId, ItemPedido item) {
                if (++chamadas == 2) {
                    throw RepositorioException.falhaTecnica("Falha simulada ao gravar o segundo item", null);
                }
                return super.inserir(conexao, pedidoId, item);
            }
        };
        PedidoService servico = pedidoService(produtoRepository, itensQueFalham);

        assertThrows(RepositorioException.class, () -> servico.criarPedido(CPF, ITENS));

        assertNadaGravado();
        assertEquals(5, estoqueNoBanco(produto2.getId()));
    }

    @Test
    @DisplayName("3) falha ao baixar estoque: rollback completo, inclusive da primeira baixa")
    void falhaAoBaixarEstoque() throws Exception {
        ProdutoRepositoryMySql produtosQueFalham = new ProdutoRepositoryMySql() {
            private int chamadas;

            @Override
            public boolean baixarEstoque(Connection conexao, Long id, int quantidade) {
                if (++chamadas == 2) {
                    throw RepositorioException.falhaTecnica("Falha simulada na baixa de estoque", null);
                }
                return super.baixarEstoque(conexao, id, quantidade);
            }
        };
        PedidoService servico = pedidoService(produtosQueFalham, itemPedidoRepository);

        assertThrows(RepositorioException.class, () -> servico.criarPedido(CPF, ITENS));

        assertNadaGravado();
        assertEquals(5, estoqueNoBanco(produto2.getId()));
    }

    @Test
    @DisplayName("4) estoque insuficiente no meio do laço: outra venda esgota o produto 2 e tudo é desfeito")
    void estoqueInsuficienteNoMeioDoLaco() throws Exception {
        // Simula uma venda concorrente (outra conexão, já confirmada) que
        // consome o estoque do produto 2 depois que o pedido o leu e enquanto
        // o item 1 está sendo processado. Ela roda antes de o item 2 ser
        // gravado, porque a gravação do item trava a linha do produto (FK).
        ProdutoRepositoryMySql produtosComVendaConcorrente = new ProdutoRepositoryMySql() {
            @Override
            public boolean baixarEstoque(Connection conexao, Long id, int quantidade) {
                if (id.equals(produto1.getId())) {
                    venderEmOutraSessao(produto2.getId(), 4);
                }
                return super.baixarEstoque(conexao, id, quantidade);
            }
        };
        PedidoService servico = pedidoService(produtosComVendaConcorrente, itemPedidoRepository);

        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
            () -> servico.criarPedido(CPF, ITENS));

        assertTrue(erro.getMessage().contains("disponível 1, solicitado 3"), erro.getMessage());
        assertNadaGravado();
        assertEquals(1, estoqueNoBanco(produto2.getId()), "só a venda concorrente permanece");
    }

    @Test
    @DisplayName("estoque insuficiente já na montagem do pedido: nada é gravado")
    void estoqueInsuficienteNaMontagem() throws Exception {
        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
            () -> pedidoService().criarPedido(CPF, List.of(
                new ItemPedidoRequest("PRD-T1", 1),
                new ItemPedidoRequest("PRD-T2", 50))));

        assertTrue(erro.getMessage().startsWith("Estoque insuficiente de Produto PRD-T2"));
        assertNadaGravado();
    }

    private void venderEmOutraSessao(Long produtoId, int quantidade) {
        try (Connection outraSessao = abrirConexao();
             PreparedStatement statement = outraSessao.prepareStatement(
                 "UPDATE produto SET quantidade_em_estoque = quantidade_em_estoque - ? WHERE id = ?")) {
            statement.setInt(1, quantidade);
            statement.setLong(2, produtoId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
