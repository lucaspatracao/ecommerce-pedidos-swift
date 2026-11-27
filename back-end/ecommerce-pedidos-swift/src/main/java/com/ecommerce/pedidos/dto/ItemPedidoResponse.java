package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.ItemPedido;
import java.math.BigDecimal;

public record ItemPedidoResponse(ProdutoResponse produto, int quantidade, BigDecimal precoPraticado) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(ProdutoResponse.de(item.getProduto()), item.getQuantidade(), item.getPrecoPraticado());
    }
}
