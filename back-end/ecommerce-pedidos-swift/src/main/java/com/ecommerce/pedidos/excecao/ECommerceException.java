package com.ecommerce.pedidos.excecao;

public class ECommerceException extends Exception {
    public ECommerceException(String mensagem) {
        super(mensagem);
    }

    public ECommerceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
