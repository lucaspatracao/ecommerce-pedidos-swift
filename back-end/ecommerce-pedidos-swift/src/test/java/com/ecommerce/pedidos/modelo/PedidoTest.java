package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.excecao.PedidoInvalidoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoTest {

    private Cliente cliente;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Ana Souza", "12345678909", "ana@email.com", "11998887766", new Endereco("Rua B", "Campinas", "13000-000"));
        pedido = new Pedido(cliente);
    }

    @Test
    @DisplayName("deve calcular valor total com varios itens")
    void deveCalcularValorTotalQuandoMultiplosItens() throws Exception {
        // Arrange
        Produto produto1 = new Produto("P-001", "Notebook", new BigDecimal("2000.00"), 5);
        Produto produto2 = new Produto("P-002", "Mouse", new BigDecimal("80.00"), 10);
        pedido.adicionarItem(produto1, 1);
        pedido.adicionarItem(produto2, 2);

        // Act
        BigDecimal total = pedido.calcularValorTotal();

        // Assert
        assertEquals(0, new BigDecimal("2160.00").compareTo(total));
    }

    @Test
    @DisplayName("deve alterar situacao para pago ao aprovar pagamento")
    void deveAlterarSituacaoParaPagoQuandoPagamentoAprovado() throws Exception {
        // Arrange
        Produto produto = new Produto("P-005", "Caderno", new BigDecimal("20.00"), 10);
        pedido.adicionarItem(produto, 1);
        Pix pix = new Pix(new BigDecimal("20.00"), "ana@email.com", "EMAIL");

        // Act
        pedido.pagar(pix);

        // Assert
        assertEquals(SituacaoPedido.PAGO, pedido.getSituacao());
        assertEquals(pix, pedido.getFormaPagamento());
    }

    @Test
    @DisplayName("deve lancar excecao quando tentar pagar pedido sem itens")
    void deveLancarExcecaoQuandoPedidoSemItens() {
        // Arrange
        Pix pix = new Pix(new BigDecimal("20.00"), "ana@email.com", "EMAIL");

        // Act
        // Assert
        assertThrows(PedidoInvalidoException.class, () -> pedido.pagar(pix));
    }

    @Test
    @DisplayName("deve manter pedido aberto quando pagamento for recusado")
    void deveManterPedidoAbertoQuandoPagamentoRecusado() throws Exception {
        // Arrange
        Produto produto = new Produto("P-010", "Fone", new BigDecimal("6000.00"), 5);
        pedido.adicionarItem(produto, 1);
        CartaoCredito cartao = new CartaoCredito(new BigDecimal("6000.00"), "**** 1234", "Visa", 1);

        // Act
        PagamentoRecusadoException excecao = assertThrows(PagamentoRecusadoException.class,
                () -> pedido.pagar(cartao));

        // Assert
        assertTrue(excecao.getMessage().contains("recusado"));
        assertEquals(SituacaoPedido.ABERTO, pedido.getSituacao());
    }
}
