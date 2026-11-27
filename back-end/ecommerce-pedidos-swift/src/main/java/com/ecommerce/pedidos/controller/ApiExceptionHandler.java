package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.excecao.ECommerceException;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ECommerceException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(ECommerceException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("erro", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", exception.getMessage()));
    }

    /**
     * Traduz o erro de persistência para HTTP olhando só o tipo. A mensagem
     * já é de negócio (sem nome de tabela ou constraint); falhas técnicas
     * ficam no log e o cliente recebe um texto genérico.
     */
    @ExceptionHandler(RepositorioException.class)
    public ResponseEntity<Map<String, String>> handleRepositorio(RepositorioException exception) {
        HttpStatus status = switch (exception.getTipo()) {
            case NAO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case VALIDACAO -> HttpStatus.BAD_REQUEST;
            case VIOLACAO_INTEGRIDADE, DUPLICADO, CONFLITO_CONCORRENCIA -> HttpStatus.CONFLICT;
            case FALHA_TECNICA -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        String mensagem = exception.getMessage();
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            LOG.error("Falha de persistência", exception);
            mensagem = "Não foi possível concluir a operação no momento. Tente novamente mais tarde.";
        }
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
