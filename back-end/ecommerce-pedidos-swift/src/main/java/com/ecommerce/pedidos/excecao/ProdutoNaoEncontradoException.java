package com.ecommerce.pedidos.excecao;

/** Especialização de 404 para produto, identificado pelo código. */
public class ProdutoNaoEncontradoException extends RecursoNaoEncontradoException {

    public ProdutoNaoEncontradoException(String codigo) {
        super("Produto", codigo);
    }
}
