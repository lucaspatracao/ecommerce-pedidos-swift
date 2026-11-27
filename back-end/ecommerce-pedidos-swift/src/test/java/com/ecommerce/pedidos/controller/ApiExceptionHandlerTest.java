package com.ecommerce.pedidos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Casos do handler difíceis de provocar por HTTP (erro inesperado,
 * concorrência). O handler é uma classe comum: basta instanciar e chamar.
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("erro inesperado vira 500 sem vazar detalhes internos")
    void deveEsconderDetalhesNo500() {
        ResponseEntity<Object> resposta = handler.tratarErroInesperado(new RuntimeException("senha do banco: x"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertEquals(Map.of("erro", "Erro interno no servidor. Tente novamente mais tarde."), resposta.getBody());
    }

    @Test
    @DisplayName("violação de integridade do banco vira 409 com mensagem de negócio")
    void deveTraduzirIntegridade() {
        ResponseEntity<Object> resposta = handler.tratarIntegridade(
            new DataIntegrityViolationException("FK", new SQLException("Cannot delete or update a parent row")));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals(Map.of("erro", "Operação viola a integridade dos dados (registro duplicado ou em uso)."),
            resposta.getBody());
    }

    @Test
    @DisplayName("conflito de versão (@Version) vira 409")
    void deveTraduzirConcorrencia() {
        ResponseEntity<Object> resposta = handler.tratarConcorrencia(
            new ObjectOptimisticLockingFailureException("Produto", 1L));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
    }

    @Test
    @DisplayName("mensagem vazia usa o texto padrão do status")
    void deveUsarTextoPadrao() {
        ResponseEntity<Object> resposta = handler.tratarConflito(new IllegalStateException());

        assertEquals(Map.of("erro", "Conflict"), resposta.getBody());
    }
}
