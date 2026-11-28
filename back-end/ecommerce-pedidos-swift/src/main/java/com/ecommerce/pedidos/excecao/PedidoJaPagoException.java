package com.ecommerce.pedidos.excecao;

/** Tentativa de pagar de novo um pedido que já está pago. Vira HTTP 409. */
public class PedidoJaPagoException extends PedidoInvalidoException {

    public PedidoJaPagoException(String numero) {
        super("Pedido " + numero + " já está pago e não pode ser pago de novo.");
    }
}
