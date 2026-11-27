package com.ecommerce.pedidos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Testes unitários da classe utilitária (Aula 03). Sem Spring. */
class UtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"90709836481", "89910440261", "907.098.364-81"})
    @DisplayName("aceita CPF com dígitos verificadores corretos, com ou sem máscara")
    void deveAceitarCpfValido(String cpf) {
        assertTrue(Utils.isCpfValido(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"90709836482", "11111111111", "1234567890", "123456789012", "abcdefghijk"})
    @DisplayName("recusa CPF com dígito errado, repetido ou tamanho inválido")
    void deveRecusarCpfInvalido(String cpf) {
        assertFalse(Utils.isCpfValido(cpf));
    }

    @Test
    @DisplayName("validarCpf devolve só os dígitos e lança exceção para CPF inválido")
    void deveNormalizarOuRecusarCpf() {
        assertEquals("90709836481", Utils.validarCpf(" 907.098.364-81 "));
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> Utils.validarCpf("90709836482"));
        assertTrue(erro.getMessage().contains("CPF inválido"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@email.com", "a.b@dominio.com.br"})
    @DisplayName("aceita e-mail no formato algo@dominio.ext")
    void deveAceitarEmail(String email) {
        assertTrue(Utils.isEmailValido(email));
        assertEquals(email, Utils.validarEmail(" " + email + " "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"anaemail.com", "ana@email", "@email.com", "ana @email.com"})
    @DisplayName("recusa e-mail mal formado")
    void deveRecusarEmail(String email) {
        assertFalse(Utils.isEmailValido(email));
        assertThrows(IllegalArgumentException.class, () -> Utils.validarEmail(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("texto obrigatório recusa nulo, vazio e só espaços")
    void deveRecusarTextoVazio(String valor) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> Utils.textoObrigatorio(valor, "Nome"));
        assertEquals("Nome é obrigatório.", erro.getMessage());
    }

    @Test
    @DisplayName("texto obrigatório remove espaços nas pontas e apenasDigitos limpa máscara")
    void deveNormalizarTexto() {
        assertEquals("abc", Utils.textoObrigatorio("  abc ", "Campo"));
        assertEquals("12345", Utils.apenasDigitos("1.2-3 4/5"));
        assertEquals("", Utils.apenasDigitos(null));
        assertFalse(Utils.isEmailValido(null));
    }

    @Test
    @DisplayName("classe utilitária não pode ser instanciada")
    void naoDeveInstanciar() throws Exception {
        Constructor<Utils> construtor = Utils.class.getDeclaredConstructor();
        construtor.setAccessible(true);
        InvocationTargetException erro = assertThrows(InvocationTargetException.class, construtor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, erro.getCause());
    }
}
