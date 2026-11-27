package com.ecommerce.pedidos.excecao;

/** Operação incompatível com a situação do pedido (ex.: pagar pedido vazio ou já pago). Vira HTTP 409. */
public class PedidoInvalidoException extends ConflitoException {

    public PedidoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
