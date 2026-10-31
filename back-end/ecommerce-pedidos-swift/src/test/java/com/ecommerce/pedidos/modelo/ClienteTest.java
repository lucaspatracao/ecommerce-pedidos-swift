package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Maria Silva", "12345678909", "maria@email.com", "11999999999", new Endereco("Rua A", "São Paulo", "01000-000"));
    }

    @Test
    @DisplayName("deve criar cliente válido quando dados forem válidos")
    void deveCriarClienteValidoQuandoDadosValidos() {
        // Arrange
        // Act
        // Assert
        assertEquals("Maria Silva", cliente.getNome());
        assertEquals("12345678909", cliente.getCpf());
        assertEquals("maria@email.com", cliente.getEmail());
        assertEquals("11999999999", cliente.getTelefone());
    }

    @Test
    @DisplayName("deve lancar excecao quando nome estiver vazio")
    void deveLancarExcecaoQuandoNomeVazio() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new Cliente("   ", "12345678909", "maria@email.com", "11999999999", new Endereco("Rua A", "São Paulo", "01000-000")));
    }

    @Test
    @DisplayName("deve lancar excecao quando email estiver sem arroba")
    void deveLancarExcecaoQuandoEmailInvalido() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new Cliente("Maria Silva", "12345678909", "mariaemail.com", "11999999999", new Endereco("Rua A", "São Paulo", "01000-000")));
    }

    @Test
    @DisplayName("deve lancar excecao quando documento for invalido")
    void deveLancarExcecaoQuandoDocumentoInvalido() {
        // Arrange
        // Act
        // Assert
        assertThrows(IllegalArgumentException.class, () -> new Cliente("Maria Silva", "12345abc", "maria@email.com", "11999999999", new Endereco("Rua A", "São Paulo", "01000-000")));
    }
}
