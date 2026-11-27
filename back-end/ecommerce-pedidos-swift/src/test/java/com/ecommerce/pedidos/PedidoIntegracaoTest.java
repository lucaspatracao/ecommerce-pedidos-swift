package com.ecommerce.pedidos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PagamentoService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PedidoIntegracaoTest {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private PagamentoService pagamentoService;

    @Test
    @DisplayName("deve criar um pedido completo e finalizar com pagamento aprovado")
    void deveCriarPedidoCompletoEFinalizarComPagamentoAprovado() {
        String cpf = "37168330535";
        clienteService.cadastrar(new ClienteRequest("Bruno Silva", cpf, "bruno@email.com", "11999998888", "Rua C"));
        produtoService.cadastrar(new ProdutoRequest("P-INT-001", "Camiseta", "", new BigDecimal("49.90"), 10, true));

        PedidoResponse pedido = pedidoService.criar(
            new PedidoRequest(cpf, List.of(new ItemPedidoRequest("P-INT-001", 2))));

        assertNotNull(pedido.numero());
        assertEquals(1, pedido.itens().size());
        assertEquals(SituacaoPedido.ABERTO, pedido.situacao());

        pagamentoService.registrar(new PagamentoRequest("PIX", new BigDecimal("99.80"), null, null, null, null, null,
            "bruno@email.com", "EMAIL", pedido.numero()));

        assertEquals(SituacaoPedido.PAGO, pedidoService.buscarPorNumero(pedido.numero()).situacao());
        assertEquals(8, produtoService.buscarPorCodigo("P-INT-001").quantidadeEmEstoque());
    }
}
