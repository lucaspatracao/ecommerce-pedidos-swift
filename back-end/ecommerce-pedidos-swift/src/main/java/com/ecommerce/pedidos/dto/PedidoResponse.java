package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * JSON de pedido devolvido pela API (mesmos campos de antes da persistência).
 * Precisa ser montado dentro da transação, porque lê cliente, itens e
 * pagamento, que são relacionamentos LAZY.
 */
public record PedidoResponse(
    String numero,
    ClienteResponse cliente,
    List<ItemPedidoResponse> itens,
    LocalDate data,
    SituacaoPedido situacao,
    Map<String, Object> formaPagamento,
    List<ItemPedidoResponse> itensOrdenadosPorSubtotal
) {

    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
            pedido.getNumero(),
            ClienteResponse.de(pedido.getCliente()),
            pedido.getItens().stream().map(ItemPedidoResponse::de).toList(),
            pedido.getData(),
            pedido.getSituacao(),
            PagamentoDetalhe.de(pedido.getFormaPagamento()),
            pedido.getItensOrdenadosPorSubtotal().stream().map(ItemPedidoResponse::de).toList()
        );
    }
}
