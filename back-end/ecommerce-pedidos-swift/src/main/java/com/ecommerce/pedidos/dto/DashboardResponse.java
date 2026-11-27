package com.ecommerce.pedidos.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resposta de {@code GET /api/dashboard}. */
public record DashboardResponse(
    long totalClientes,
    long totalPedidos,
    long totalProdutos,
    BigDecimal receitaTotal,
    List<ProdutoResponse> estoqueCritico,
    List<PedidoResponse> pedidosRecentes
) {
}
