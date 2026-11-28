package com.ecommerce.pedidos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ecommerce.pedidos.dto.ErroResponse;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Casos do handler difíceis de provocar por HTTP (concorrência, integridade).
 * O handler é uma classe comum: basta instanciar e chamar.
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/pedidos");

    private static ErroResponse corpo(ResponseEntity<Object> resposta) {
        return (ErroResponse) resposta.getBody();
    }

    @Test
    @DisplayName("erro inesperado vira 500 com mensagem genérica, sem detalhe interno")
    void deveEsconderDetalhesNo500() {
        ResponseEntity<Object> resposta = handler.tratarErroInesperado(
            new RuntimeException("senha do banco: x"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertEquals(new ErroResponse(500, "Erro interno", "Erro interno. Tente novamente mais tarde.", "/api/pedidos"),
            corpo(resposta));
    }

    @Test
    @DisplayName("violação de integridade vira 409 sem repassar o texto do banco")
    void deveTraduzirIntegridade() {
        ResponseEntity<Object> resposta = handler.tratarIntegridade(
            new DataIntegrityViolationException("FK", new SQLException("Cannot delete or update a parent row")),
            request);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals("Conflito", corpo(resposta).erro());
        assertFalse(corpo(resposta).mensagem().contains("parent row"));
        assertNull(corpo(resposta).campos());
    }

    @Test
    @DisplayName("conflito de versão (@Version) vira 409")
    void deveTraduzirConcorrencia() {
        ResponseEntity<Object> resposta = handler.tratarConcorrencia(
            new ObjectOptimisticLockingFailureException("Produto", 1L), request);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals(409, corpo(resposta).status());
    }

    @Test
    @DisplayName("mensagem vazia usa o rótulo do status")
    void deveUsarTextoPadrao() {
        ResponseEntity<Object> resposta = handler.tratarConflito(new IllegalStateException(), request);

        assertEquals("Conflito", corpo(resposta).mensagem());
    }

    @Test
    @DisplayName("rótulos em português; status sem rótulo próprio usa a frase padrão")
    void deveRotularStatus() {
        assertEquals("Requisição inválida", ApiExceptionHandler.rotulo(HttpStatus.BAD_REQUEST));
        assertEquals("Não encontrado", ApiExceptionHandler.rotulo(HttpStatus.NOT_FOUND));
        assertEquals("Método não permitido", ApiExceptionHandler.rotulo(HttpStatus.METHOD_NOT_ALLOWED));
        assertEquals("Tipo de conteúdo não suportado", ApiExceptionHandler.rotulo(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
        assertEquals("Forbidden", ApiExceptionHandler.rotulo(HttpStatus.FORBIDDEN));
    }
}
