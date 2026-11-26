package com.ecommerce.pedidos.excecao;

/**
 * A requisição é válida, mas conflita com o estado atual dos dados
 * (registro duplicado, estoque acabou, pedido já pago...). Vira HTTP 409.
 */
public class ConflitoException extends NegocioException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    public ConflitoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
