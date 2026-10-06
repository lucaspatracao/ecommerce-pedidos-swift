package com.ecommerce.pedidos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.excecao.PedidoInvalidoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PagamentoTest {

    @Test
    @DisplayName("deve processar pagamento via pix com sucesso")
    void deveProcessarPagamentoViaPixQuandoDadosValidos() {
        // Arrange
        Pix pix = new Pix(new BigDecimal("150.00"), "cliente@teste.com", "EMAIL");

        // Act
        boolean processado = pix.processar();

        // Assert
        assertTrue(processado);
        assertEquals(SituacaoPagamento.APROVADO, pix.getSituacao());
    }

    @Test
    @DisplayName("deve processar pagamento via boleto com sucesso")
    void deveProcessarPagamentoViaBoletoQuandoDadosValidos() {
        // Arrange
        Boleto boleto = new Boleto(new BigDecimal("250.00"), LocalDate.now().plusDays(5), "123456789012345");

        // Act
        boolean processado = boleto.processar();

        // Assert
        assertTrue(processado);
        assertEquals(SituacaoPagamento.APROVADO, boleto.getSituacao());
    }

    @Test
    @DisplayName("deve processar pagamento via cartao com sucesso")
    void deveProcessarPagamentoViaCartaoQuandoDadosValidos() {
        // Arrange
        CartaoCredito cartao = new CartaoCredito(new BigDecimal("200.00"), "**** 1234", "Visa", 3);

        // Act
        boolean processado = cartao.processar();

        // Assert
        assertTrue(processado);
        assertEquals(SituacaoPagamento.APROVADO, cartao.getSituacao());
    }

    @Test
    @DisplayName("deve recusar pagamento do cartao quando limite for ultrapassado")
    void deveRecusarPagamentoViaCartaoQuandoLimiteUltrapassado() {
        // Arrange
        CartaoCredito cartao = new CartaoCredito(new BigDecimal("6000.00"), "**** 1234", "Visa", 1);

        // Act
        boolean processado = cartao.processar();

        // Assert
        assertFalse(processado);
        assertEquals(SituacaoPagamento.RECUSADO, cartao.getSituacao());
    }

    @Test
    @DisplayName("deve lancar excecao quando pagamento for recusado pelo pedido")
    void deveLancarExcecaoQuandoPedidoComPagamentoRecusado() throws Exception {
        // Arrange
        Cliente cliente = new Cliente("Pedro", "22233344455", "pedro@email.com", "11987654321", new Endereco("Rua C", "Rio", "20000-000"));
        Pedido pedido = new Pedido(cliente);
        Produto produto = new Produto("P-020", "Tablet", new BigDecimal("6000.00"), 5);
        pedido.adicionarItem(produto, 1);
        CartaoCredito cartao = new CartaoCredito(new BigDecimal("6000.00"), "**** 4321", "Mastercard", 2);

        // Act
        // Assert
        PagamentoRecusadoException excecao = assertThrows(PagamentoRecusadoException.class, () -> {
            pedido.pagar(cartao);
        });
        assertEquals(SituacaoPedido.ABERTO, pedido.getSituacao());
        assertTrue(excecao.getMessage().contains("recusado"));
    }
}
