package com.ecommerce.pedidos.excecao;

/**
 * Já existe registro com o mesmo identificador natural ou campo único
 * (CPF ou e-mail de cliente, código de produto). Vira HTTP 409.
 */
public class RegistroDuplicadoException extends ConflitoException {

    public RegistroDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
