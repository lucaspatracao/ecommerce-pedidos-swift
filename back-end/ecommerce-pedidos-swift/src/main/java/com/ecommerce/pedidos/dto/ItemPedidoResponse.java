package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.ItemPedido;
import java.math.BigDecimal;

/**
 * Item do pedido no JSON. {@code subtotal} é campo novo: vem pronto de
 * {@link ItemPedido#calcularSubtotal()}, para o front não refazer a conta.
 */
public record ItemPedidoResponse(ProdutoResponse produto, int quantidade, BigDecimal precoPraticado, BigDecimal subtotal) {

    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(ProdutoResponse.de(item.getProduto()), item.getQuantidade(),
            item.getPrecoPraticado(), item.calcularSubtotal());
    }
}
