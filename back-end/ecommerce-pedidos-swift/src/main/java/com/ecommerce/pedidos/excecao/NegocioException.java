package com.ecommerce.pedidos.excecao;

/**
 * Raiz de todas as exceções de regra de negócio do sistema (Aula 09).
 *
 * <p>É <b>unchecked</b> ({@code RuntimeException}) por dois motivos:</p>
 * <ol>
 *   <li>quem chama normalmente não tem como "consertar" o erro ali mesmo; o
 *       certo é deixar subir até o {@code ApiExceptionHandler}, que o traduz
 *       em resposta HTTP;</li>
 *   <li>o Spring só faz rollback automático de {@code @Transactional} para
 *       exceções unchecked. Com uma exceção checked, um pedido que falhasse
 *       no meio deixaria o estoque já baixado no banco.</li>
 * </ol>
 *
 * <p>Lançada diretamente, vira HTTP 400. As subclasses refinam o status:
 * {@link RecursoNaoEncontradoException} → 404 e {@link ConflitoException} → 409.</p>
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }

    public NegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
