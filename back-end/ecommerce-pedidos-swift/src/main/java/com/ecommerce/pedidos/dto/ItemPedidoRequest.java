package com.ecommerce.pedidos.dto;

/** Um item dentro de {@link PedidoRequest}: código do produto e quantidade. */
public record ItemPedidoRequest(String codigo, int quantidade) {
}
