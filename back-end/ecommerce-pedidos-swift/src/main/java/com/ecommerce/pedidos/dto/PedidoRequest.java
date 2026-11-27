package com.ecommerce.pedidos.dto;

import java.util.List;

/** Corpo de POST {@code /api/pedidos}. */
public record PedidoRequest(String clienteCpf, List<ItemPedidoRequest> itens) {
}
