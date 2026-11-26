package com.ecommerce.pedidos.excecao;

/** Especialização de 404 para cliente, identificado pelo CPF. */
public class ClienteNaoEncontradoException extends RecursoNaoEncontradoException {

    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente", cpf);
    }
}
