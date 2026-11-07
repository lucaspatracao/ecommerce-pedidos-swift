package com.ecommerce.pedidos.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.apoio.BancoEmMemoria;
import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Pix;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.RepositorioException.Tipo;
import com.ecommerce.pedidos.servico.PedidoService.ItemPedidoRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regras dos serviços testadas sem banco, sobre os repositórios em memória. */
class ServicosComRepositorioEmMemoriaTest {
    private BancoEmMemoria banco;
    private ClienteService clienteService;
    private ProdutoService produtoService;
    private PedidoService pedidoService;

    @BeforeEach
    void preparar() {
        banco = new BancoEmMemoria();
        clienteService = banco.clienteService();
        produtoService = banco.produtoService();
        pedidoService = banco.pedidoService();
        clienteService.salvar(new Cliente("Iara", "88899900011", "iara@email.com", "11977776666", "Rua A"));
        produtoService.salvar(new Produto("PRD-MEM", "Livro", new BigDecimal("30.00"), 4));
    }

    @Test
    @DisplayName("atualizar cliente inexistente falha com NAO_ENCONTRADO")
    void atualizarClienteInexistente() {
        Cliente qualquer = new Cliente("X", "10101010101", "x@email.com", "11000000000", "Rua X");
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> clienteService.atualizar("10101010101", qualquer));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("produto excluído pela API é apenas inativado e pode ser reativado")
    void excluirProdutoInativa() {
        produtoService.excluir("PRD-MEM");

        assertTrue(produtoService.listarTodos().isEmpty());
        assertEquals("PRD-MEM", produtoService.reativar("PRD-MEM").getCodigo());
        assertEquals(1, produtoService.contar());
    }

    @Test
    @DisplayName("pedido pago não pode ser excluído")
    void pedidoPagoNaoPodeSerExcluido() throws Exception {
        Pedido pedido = pedidoService.criarPedido("88899900011", List.of(new ItemPedidoRequest("PRD-MEM", 1)));
        pedidoService.registrarPagamento(pedido.getNumero(), new Pix(new BigDecimal("30.00"), "iara@email.com", "EMAIL"));

        assertThrows(IllegalStateException.class, () -> pedidoService.excluir(pedido.getNumero()));
        assertEquals(1, pedidoService.contar());
    }

    @Test
    @DisplayName("estoque insuficiente lança EstoqueInsuficienteException com mensagem clara")
    void estoqueInsuficiente() {
        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
            () -> pedidoService.criarPedido("88899900011", List.of(new ItemPedidoRequest("PRD-MEM", 9))));
        assertEquals("Estoque insuficiente de Livro: disponível 4, solicitado 9", erro.getMessage());
    }

    @Test
    @DisplayName("pedido com cliente inexistente falha com NAO_ENCONTRADO")
    void clienteInexistente() {
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> pedidoService.criarPedido("00000000000", List.of(new ItemPedidoRequest("PRD-MEM", 1))));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }
}
