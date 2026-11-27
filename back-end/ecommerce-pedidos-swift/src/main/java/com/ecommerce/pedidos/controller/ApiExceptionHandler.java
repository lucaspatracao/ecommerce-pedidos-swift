package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.excecao.NegocioException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduz exceções em respostas HTTP (Aula 09).
 *
 * <p>Toda resposta de erro tem o formato {@code { "erro": "mensagem" }},
 * porque é essa chave que o front-end lê em {@code src/api/clienteApi.js}.</p>
 *
 * <p>Tabela de tradução:</p>
 * <pre>
 *  RecursoNaoEncontradoException ........ 404
 *  ConflitoException (e subclasses) ..... 409  estoque, pedido inválido, pagamento recusado, duplicado
 *  NegocioException (demais) ............ 400
 *  IllegalArgumentException ............. 400  validação do domínio (CPF, preço, quantidade...)
 *  IllegalStateException ................ 409  operação incompatível com o estado do objeto
 *  DataIntegrityViolation / lock otimista 409  o banco recusou (duplicado, em uso, concorrência)
 *  erros do próprio Spring MVC .......... status original (JSON inválido 400, rota inexistente 404...)
 *  qualquer outra exceção ............... 500  mensagem genérica; o detalhe vai só para o log
 * </pre>
 *
 * <p>Quando mais de um método serve, o Spring escolhe o da exceção mais
 * específica. Por isso {@code ConflitoException} cai no 409 mesmo sendo
 * também uma {@code NegocioException}.</p>
 *
 * <p>Herdar de {@link ResponseEntityExceptionHandler} faz o Spring tratar as
 * exceções internas dele (JSON malformado, método HTTP não suportado...);
 * aqui só trocamos o corpo da resposta para o formato {@code {erro}}.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ConflitoException.class, IllegalStateException.class})
    public ResponseEntity<Object> tratarConflito(RuntimeException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({NegocioException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> tratarRequisicaoInvalida(RuntimeException ex) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * O banco recusou a gravação (UNIQUE, FOREIGN KEY, CHECK). Os serviços já
     * checam os casos comuns antes; isto cobre corridas entre duas requisições.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> tratarIntegridade(DataIntegrityViolationException ex) {
        LOG.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return erro(HttpStatus.CONFLICT, "Operação viola a integridade dos dados (registro duplicado ou em uso).");
    }

    /** Outra transação alterou o mesmo registro antes (ver @Version em Produto). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> tratarConcorrencia(ObjectOptimisticLockingFailureException ex) {
        return erro(HttpStatus.CONFLICT, "Os dados foram alterados por outra operação. Recarregue e tente novamente.");
    }

    /** Ex.: texto onde a rota espera número. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'.");
    }

    /** Última rede de proteção: nunca devolve stack trace ao cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarErroInesperado(Exception ex) {
        LOG.error("Erro inesperado ao processar a requisição", ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor. Tente novamente mais tarde.");
    }

    /** JSON mal formado ou com tipo errado (ex.: texto em campo numérico). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado.");
    }

    /** Rota que não existe na API. */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers,
                                                                    HttpStatusCode status, WebRequest request) {
        return erro(HttpStatus.NOT_FOUND, "Rota não encontrada: /" + ex.getResourcePath());
    }

    /**
     * Ponto único por onde passam as exceções internas do Spring MVC.
     * O Spring já escolheu o status; só reescrevemos o corpo no formato {@code {erro}}.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String mensagem = body instanceof ProblemDetail detalhe && detalhe.getDetail() != null
            ? detalhe.getDetail()
            : ex.getMessage();
        return ResponseEntity.status(statusCode).headers(headers).body(Map.of("erro", mensagem));
    }

    private static ResponseEntity<Object> erro(HttpStatus status, String mensagem) {
        String texto = mensagem == null || mensagem.isBlank() ? status.getReasonPhrase() : mensagem;
        return ResponseEntity.status(status).body(Map.of("erro", texto));
    }
}
