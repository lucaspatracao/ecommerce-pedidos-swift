package com.ecommerce.pedidos.excecao;

/** Especialização de 404 para pedido, identificado pelo número. */
public class PedidoNaoEncontradoException extends RecursoNaoEncontradoException {

    public PedidoNaoEncontradoException(String numero) {
        super("Pedido", numero);
    }
}
