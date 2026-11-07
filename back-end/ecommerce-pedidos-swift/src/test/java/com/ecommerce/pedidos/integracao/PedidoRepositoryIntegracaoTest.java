package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Pix;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.RepositorioException.Tipo;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.PedidoService.ItemPedidoRequest;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoRepositoryIntegracaoTest extends IntegracaoBancoBase {
    private static final String CPF = "66677788899";

    private PedidoService pedidoService;
    private Produto camiseta;
    private Produto caneca;

    @BeforeEach
    void preparar() {
        pedidoService = pedidoService();
        criarCliente(CPF, "gabi@email.com");
        camiseta = criarProduto("PRD-CAM", "50.00", 10);
        caneca = criarProduto("PRD-CAN", "20.00", 10);
    }

    private Pedido novoPedido() throws Exception {
        return pedidoService.criarPedido(CPF, List.of(
            new ItemPedidoRequest("PRD-CAM", 2),
            new ItemPedidoRequest("PRD-CAN", 1)));
    }

    @Test
    @DisplayName("cascade: excluir pedido remove seus itens")
    void excluirPedidoRemoveItens() throws Exception {
        Pedido pedido = novoPedido();
        assertEquals(2, contar("item_pedido"));

        transacao.executar(conexao -> {
            pedidoRepository.remover(conexao, pedido.getId());
            return null;
        });

        assertEquals(0, contar("pedido"));
        assertEquals(0, contar("item_pedido"));
    }

    @Test
    @DisplayName("excluir pedido em aberto pelo serviço devolve o estoque reservado")
    void excluirPedidoAbertoDevolveEstoque() throws Exception {
        Pedido pedido = novoPedido();
        assertEquals(8, estoqueNoBanco(camiseta.getId()));

        pedidoService.excluir(pedido.getNumero());

        assertEquals(10, estoqueNoBanco(camiseta.getId()));
        assertEquals(10, estoqueNoBanco(caneca.getId()));
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> pedidoService.buscarPorNumero(pedido.getNumero()));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("pedido já pago não pode ser excluído: deve ser cancelado")
    void pedidoPagoNaoPodeSerExcluido() throws Exception {
        Pedido pedido = novoPedido();
        pedidoService.registrarPagamento(pedido.getNumero(), new Pix(new BigDecimal("120.00"), "gabi@email.com", "EMAIL"));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
            () -> pedidoService.excluir(pedido.getNumero()));
        assertTrue(erro.getMessage().contains("Cancele o pedido"));
        assertEquals(1, contar("pedido"));

        Pedido cancelado = pedidoService.cancelar(pedido.getNumero());
        assertEquals(SituacaoPedido.CANCELADO, cancelado.getSituacao());
        assertEquals(SituacaoPedido.CANCELADO, pedidoService.buscarPorNumero(pedido.getNumero()).getSituacao());
        assertEquals(10, estoqueNoBanco(camiseta.getId()));
    }

    @Test
    @DisplayName("atualizar status: pagar grava PAGO e cancelar duas vezes é recusado")
    void atualizarStatus() throws Exception {
        Pedido pedido = novoPedido();

        pedidoService.registrarPagamento(pedido.getNumero(), new Pix(new BigDecimal("120.00"), "gabi@email.com", "EMAIL"));
        assertEquals(SituacaoPedido.PAGO, pedidoService.buscarPorNumero(pedido.getNumero()).getSituacao());
        assertEquals(1, pedidoService.listarPorSituacao(SituacaoPedido.PAGO, Pagina.todas()).size());

        pedidoService.atualizarStatus(pedido.getNumero(), SituacaoPedido.CANCELADO);
        assertThrows(IllegalStateException.class, () -> pedidoService.cancelar(pedido.getNumero()));
    }

    @Test
    @DisplayName("atualizar situação de pedido inexistente falha com NAO_ENCONTRADO")
    void atualizarSituacaoInexistente() {
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> {
                pedidoRepository.atualizarSituacao(conexao, 5555L, SituacaoPedido.ABERTO, SituacaoPedido.CANCELADO);
                return null;
            }));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("histórico: pedido antigo mantém produto e preço cobrados mesmo com produto inativo e preço alterado")
    void pedidoAntigoMantemProdutoEPreco() throws Exception {
        Pedido pedido = novoPedido();

        ProdutoService produtoService = new ProdutoService(produtoRepository, transacao);
        Produto reajustado = new Produto("PRD-CAM", "Camiseta Nova", new BigDecimal("80.00"), 8);
        produtoService.atualizar("PRD-CAM", reajustado);
        produtoService.inativar("PRD-CAM");

        Pedido relido = pedidoService.buscarPorNumero(pedido.getNumero());
        ItemPedido itemCamiseta = relido.getItens().stream()
            .filter(item -> item.getProduto().getCodigo().equals("PRD-CAM")).findFirst().orElseThrow();
        assertEquals(new BigDecimal("50.00"), itemCamiseta.getPrecoPraticado());
        assertFalse(itemCamiseta.getProduto().isAtivo());
        assertEquals(new BigDecimal("120.00"), relido.calcularValorTotal());
    }

    @Test
    @DisplayName("remover item de pedido aberto devolve o estoque do item")
    void removerItem() throws Exception {
        Pedido pedido = novoPedido();
        Long itemCaneca = pedido.getItens().stream()
            .filter(item -> item.getProduto().getCodigo().equals("PRD-CAN")).findFirst().orElseThrow().getId();

        Pedido atualizado = pedidoService.removerItem(pedido.getNumero(), itemCaneca);

        assertEquals(1, atualizado.quantidadeDeItens());
        assertEquals(10, estoqueNoBanco(caneca.getId()));
        long itensRestantes = transacao.executar(conexao -> itemPedidoRepository.contarPorPedido(conexao, pedido.getId()));
        assertEquals(1, itensRestantes);
    }

    @Test
    @DisplayName("listagens por cliente e paginação")
    void listagens() throws Exception {
        novoPedido();
        novoPedido();

        assertEquals(2, pedidoService.listarPorCliente(CPF, Pagina.todas()).size());
        assertEquals(1, pedidoService.listar(Pagina.de(0, 1)).size());
        assertEquals(2, pedidoService.contar());
    }
}
