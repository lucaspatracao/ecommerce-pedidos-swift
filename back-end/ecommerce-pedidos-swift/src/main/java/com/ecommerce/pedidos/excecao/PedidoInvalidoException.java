package com.ecommerce.pedidos.excecao;

public class PedidoInvalidoException extends ECommerceException {
    private final String numeroPedido;

    public PedidoInvalidoException(String numeroPedido, String motivo) {
        super("Pedido " + numeroPedido + " inválido: " + motivo);
        this.numeroPedido = numeroPedido;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }
}
