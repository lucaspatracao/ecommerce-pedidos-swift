package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.CampoInvalido;
import com.ecommerce.pedidos.dto.ErroResponse;
import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.excecao.NegocioException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduz exceções em respostas HTTP com o corpo padronizado {@link ErroResponse} (Aula 15).
 *
 * <p>Tabela de tradução:</p>
 * <pre>
 *  RecursoNaoEncontradoException (e subclasses) ... 404  cliente, produto, pedido, pagamento
 *  ConflitoException (e subclasses) ............... 409  duplicado, estoque, já pago, pagamento recusado
 *  NegocioException (demais) ...................... 400
 *  Bean Validation (@Valid) ....................... 400  com a lista "campos"
 *  JSON malformado ................................ 400
 *  IllegalArgumentException ....................... 400  validação do domínio (CPF, preço...)
 *  IllegalStateException .......................... 409  operação incompatível com o estado do objeto
 *  DataIntegrityViolation / lock otimista ......... 409  o banco recusou (duplicado, em uso, concorrência)
 *  demais erros do Spring MVC ..................... status original (405, 415, 404 de rota...)
 *  qualquer outra exceção ......................... 500  mensagem genérica; o detalhe vai só para o log
 * </pre>
 *
 * <p>Quando mais de um método serve, o Spring escolhe o da exceção mais
 * específica. Por isso {@code ConflitoException} cai no 409 mesmo sendo
 * também uma {@code NegocioException}, e um só handler atende todos os 404.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    static final String MENSAGEM_ERRO_INTERNO = "Erro interno. Tente novamente mais tarde.";
    static final String MENSAGEM_VALIDACAO = "Há campos inválidos. Corrija-os e tente novamente.";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({ConflitoException.class, IllegalStateException.class})
    public ResponseEntity<Object> tratarConflito(RuntimeException ex, HttpServletRequest request) {
        return erro(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({NegocioException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> tratarRequisicaoInvalida(RuntimeException ex, HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * O banco recusou a gravação (UNIQUE, FOREIGN KEY, CHECK). Os serviços já
     * checam os casos comuns antes; isto cobre corridas entre duas requisições.
     * O texto do banco (com SQL e nomes de índice) fica só no log.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        LOG.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return erro(HttpStatus.CONFLICT, "Operação viola a integridade dos dados (registro duplicado ou em uso).",
            request.getRequestURI());
    }

    /** Outra transação alterou o mesmo registro antes (ver @Version em Produto). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> tratarConcorrencia(ObjectOptimisticLockingFailureException ex,
                                                     HttpServletRequest request) {
        return erro(HttpStatus.CONFLICT, "Os dados foram alterados por outra operação. Recarregue e tente novamente.",
            request.getRequestURI());
    }

    /** Ex.: texto onde a rota espera número. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                     HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'.",
            request.getRequestURI());
    }

    /**
     * Última rede de proteção. O cliente recebe só a mensagem genérica: nada de
     * nome de classe, SQL ou stack trace. O detalhe completo vai para o log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        LOG.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO, request.getRequestURI());
    }

    /**
     * Falha do Bean Validation num {@code @Valid @RequestBody}: 400 com a
     * lista {@code campos}, para o front mostrar cada mensagem junto do campo.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(campo -> new CampoInvalido(campo.getField(), campo.getDefaultMessage()))
            .distinct()
            .toList();
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(badRequest)
            .body(new ErroResponse(badRequest.value(), rotulo(badRequest), MENSAGEM_VALIDACAO, caminho(request), campos));
    }

    /** JSON mal formado ou com tipo errado (ex.: texto em campo numérico). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado.", caminho(request));
    }

    /** Rota que não existe na API. */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers,
                                                                    HttpStatusCode status, WebRequest request) {
        return erro(HttpStatus.NOT_FOUND, "Rota não encontrada: /" + ex.getResourcePath(), caminho(request));
    }

    /**
     * Ponto único por onde passam as demais exceções internas do Spring MVC
     * (405, 415...). O Spring já escolheu o status; só trocamos o corpo.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String mensagem = body instanceof ProblemDetail detalhe && detalhe.getDetail() != null
            ? detalhe.getDetail()
            : ex.getMessage();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String rotulo = status == null ? String.valueOf(statusCode.value()) : rotulo(status);
        return ResponseEntity.status(statusCode).headers(headers)
            .body(new ErroResponse(statusCode.value(), rotulo, textoOuPadrao(mensagem, rotulo), caminho(request)));
    }

    /** Rótulo do status em português; para os demais, a frase padrão do HTTP. */
    static String rotulo(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case NOT_FOUND -> "Não encontrado";
            case METHOD_NOT_ALLOWED -> "Método não permitido";
            case CONFLICT -> "Conflito";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de conteúdo não suportado";
            case INTERNAL_SERVER_ERROR -> "Erro interno";
            default -> status.getReasonPhrase();
        };
    }

    private static ResponseEntity<Object> erro(HttpStatus status, String mensagem, String caminho) {
        String rotulo = rotulo(status);
        return ResponseEntity.status(status)
            .body(new ErroResponse(status.value(), rotulo, textoOuPadrao(mensagem, rotulo), caminho));
    }

    private static String textoOuPadrao(String mensagem, String padrao) {
        return mensagem == null || mensagem.isBlank() ? padrao : mensagem;
    }

    private static String caminho(WebRequest request) {
        return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : null;
    }
}
