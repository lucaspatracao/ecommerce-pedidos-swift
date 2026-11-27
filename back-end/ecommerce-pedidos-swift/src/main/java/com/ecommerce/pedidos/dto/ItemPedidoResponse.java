package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.ItemPedido;
import java.math.BigDecimal;

/** Item do pedido. O {@code id} identifica o item em DELETE /pedidos/{numero}/itens/{id}. */
public record ItemPedidoResponse(Long id, ProdutoResponse produto, int quantidade, BigDecimal precoPraticado) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(item.getId(), ProdutoResponse.de(item.getProduto()), item.getQuantidade(),
            item.getPrecoPraticado());
    }
}
