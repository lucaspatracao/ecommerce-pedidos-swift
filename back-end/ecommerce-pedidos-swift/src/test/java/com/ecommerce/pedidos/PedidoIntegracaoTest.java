package com.ecommerce.pedidos;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Endereco;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Pix;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import com.ecommerce.pedidos.servico.ClienteService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoIntegracaoTest {

    @Test
    @DisplayName("deve criar um pedido completo e finalizar com pagamento aprovado")
    void deveCriarPedidoCompletoEFinalizarComPagamentoAprovado() {
        ClienteService clienteService = new ClienteService();
        ProdutoService produtoService = new ProdutoService();
        PedidoService pedidoService = new PedidoService(clienteService, produtoService);

        Cliente cliente = new Cliente("Bruno Silva", "12345678900", "bruno@email.com", "11999998888",
                new Endereco("Rua C", "São Paulo", "01000-001"));
        clienteService.salvar(cliente);

        Produto produto = new Produto("P-INT-001", "Camiseta", new BigDecimal("49.90"), 10);
        produtoService.salvar(produto);

        Pedido pedido = assertDoesNotThrow(() -> pedidoService.criarPedido(cliente.getCpf(),
                List.of(new PedidoService.ItemPedidoRequest(produto.getCodigo(), 2))));

        assertNotNull(pedido);
        assertEquals(1, pedido.quantidadeDeItens());
        assertEquals(SituacaoPedido.ABERTO, pedido.getSituacao());

        Pix pix = new Pix(new BigDecimal("99.80"), "bruno@email.com", "EMAIL");
        assertDoesNotThrow(() -> pedido.pagar(pix));
        assertEquals(SituacaoPedido.PAGO, pedido.getSituacao());
    }
}
