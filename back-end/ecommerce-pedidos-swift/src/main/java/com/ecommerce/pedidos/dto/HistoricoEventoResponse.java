package com.ecommerce.pedidos.dto;

/** Item de {@code GET /api/historico}. */
public record HistoricoEventoResponse(String titulo, String descricao, String data) {

    public static HistoricoEventoResponse de(PedidoResponse pedido) {
        return new HistoricoEventoResponse(
            "Pedido " + pedido.numero() + " criado",
            "Cliente: " + pedido.cliente().nome() + " | Status: " + pedido.situacao(),
            pedido.data().toString()
        );
    }
}
