package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProdutoTest {

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto("P-001", "Teclado Mecânico", new BigDecimal("299.90"), 10);
    }

    @Test
    @DisplayName("deve criar produto válido e manter estoque inicial")
    void deveCriarProdutoValidoQuandoDadosValidos() {
        // Arrange
        // Act
        // Assert
        assertEquals("P-001", produto.getCodigo());
        assertEquals("Teclado Mecânico", produto.getNome());
        assertEquals(0, new BigDecimal("299.90").compareTo(produto.getPreco()));
        assertEquals(10, produto.getQuantidadeEmEstoque());
        assertTrue(produto.isAtivo());
    }

    @Test
    @DisplayName("deve baixar estoque quando quantidade for suficiente")
    void deveBaixarEstoqueQuandoQuantidadeDisponivel() throws EstoqueInsuficienteException {
        // Arrange
        int quantidade = 3;

        // Act
        produto.baixarEstoque(quantidade);

        // Assert
        assertEquals(7, produto.getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("deve repor estoque quando quantidade positiva")
    void deveAdicionarEstoqueQuandoQuantidadePositiva() {
        // Arrange
        int quantidade = 4;

        // Act
        produto.adicionarEstoque(quantidade);

        // Assert
        assertEquals(14, produto.getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("deve lancar excecao quando estoque for insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        // Arrange
        int quantidadeSolicitada = 11;

        // Act
        EstoqueInsuficienteException excecao = assertThrows(EstoqueInsuficienteException.class,
                () -> produto.baixarEstoque(quantidadeSolicitada));

        // Assert
        assertEquals(10, produto.getQuantidadeEmEstoque());
        assertEquals("Estoque insuficiente de Teclado Mecânico", excecao.getMessage().substring(0, 40));
    }

    @Test
    @DisplayName("deve lancar excecao quando preco for menor ou igual a zero")
    void deveLancarExcecaoQuandoPrecoNaoEhValido() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new Produto("P-002", "Mouse", BigDecimal.ZERO, 5));
        assertThrows(IllegalArgumentException.class, () -> new Produto("P-003", "Monitor", new BigDecimal("-10.00"), 5));
    }

    @Test
    @DisplayName("deve lancar excecao quando nome for vazio")
    void deveLancarExcecaoQuandoNomeInvalido() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new Produto("P-004", "   ", new BigDecimal("15.00"), 5));
    }
}
