package com.ecommerce.pedidos.excecao;

/** O pedido existe, mas ainda não tem pagamento. Vira HTTP 404 em GET /api/pedidos/{numero}/pagamento. */
public class PagamentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public PagamentoNaoEncontradoException(String numeroPedido) {
        super("Pagamento do pedido", numeroPedido);
    }
}
