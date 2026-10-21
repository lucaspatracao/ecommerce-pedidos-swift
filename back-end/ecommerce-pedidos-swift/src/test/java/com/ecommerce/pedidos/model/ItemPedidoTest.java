package com.ecommerce.pedidos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemPedidoTest {

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto("P-001", "Camiseta", new BigDecimal("49.90"), 5);
    }

    @Test
    @DisplayName("deve calcular subtotal corretamente quando quantidade for positiva")
    void deveCalcularSubtotalQuandoQuantidadeValida() {
        // Arrange
        ItemPedido itemPedido = new ItemPedido(produto, 3);

        // Act
        BigDecimal subtotal = itemPedido.calcularSubtotal();

        // Assert
        assertEquals(0, new BigDecimal("149.70").compareTo(subtotal));
    }

    @Test
    @DisplayName("deve lancar excecao quando quantidade for zero")
    void deveLancarExcecaoQuandoQuantidadeZerada() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(produto, 0));
    }

    @Test
    @DisplayName("deve lancar excecao quando quantidade for negativa")
    void deveLancarExcecaoQuandoQuantidadeNegativa() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(produto, -2));
    }
}
