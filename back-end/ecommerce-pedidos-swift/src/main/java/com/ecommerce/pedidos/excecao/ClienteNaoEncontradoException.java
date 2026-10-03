package com.ecommerce.pedidos.excecao;

public class ClienteNaoEncontradoException extends ECommerceException {
    private final String identificador;

    public ClienteNaoEncontradoException(String identificador) {
        super("Cliente não encontrado: " + identificador);
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }
}
