package com.ecommerce.pedidos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("registro inexistente vira 404")
    void naoEncontradoVira404() {
        var resposta = handler.handleRepositorio(RepositorioException.naoEncontrado("Cliente", "123"));
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    @Test
    @DisplayName("violação de integridade vira 409 com a mensagem de negócio")
    void violacaoVira409() {
        var resposta = handler.handleRepositorio(RepositorioException.violacaoIntegridade(
            "Este cliente possui pedidos e não pode ser excluído.", new SQLException("fk", "23000", 1451)));
        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals("Este cliente possui pedidos e não pode ser excluído.", resposta.getBody().get("erro"));
    }

    @Test
    @DisplayName("duplicidade e conflito de versão viram 409; validação vira 400")
    void demaisTipos() {
        assertEquals(HttpStatus.CONFLICT,
            handler.handleRepositorio(RepositorioException.duplicado("dup", null)).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
            handler.handleRepositorio(RepositorioException.conflitoConcorrencia("Produto", "P1")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
            handler.handleRepositorio(RepositorioException.validacao("sem id")).getStatusCode());
    }

    @Test
    @DisplayName("falha técnica vira 500 sem vazar detalhes do banco")
    void falhaTecnicaNaoVazaDetalhes() {
        var resposta = handler.handleRepositorio(RepositorioException.falhaTecnica(
            "Erro ao acessar o banco de dados.", new SQLException("Table 'ecommerce.produto' doesn't exist")));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertFalse(resposta.getBody().get("erro").contains("produto"));
    }
}
