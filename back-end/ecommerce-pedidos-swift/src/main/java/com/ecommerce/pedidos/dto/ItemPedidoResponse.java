package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.ItemPedido;
import java.math.BigDecimal;

/**
 * Item do pedido no JSON.
 *
 * <p>{@code id} identifica o item em DELETE /pedidos/{numero}/itens/{id}.
 * {@code subtotal} vem pronto de {@link ItemPedido#calcularSubtotal()}, para
 * o front não refazer a conta.</p>
 */
public record ItemPedidoResponse(Long id, ProdutoResponse produto, int quantidade, BigDecimal precoPraticado,
                                 BigDecimal subtotal) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(item.getId(), ProdutoResponse.de(item.getProduto()), item.getQuantidade(),
            item.getPrecoPraticado(), item.calcularSubtotal());
    }
}
