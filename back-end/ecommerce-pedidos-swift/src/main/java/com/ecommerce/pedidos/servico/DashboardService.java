package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.DashboardResponse;

/** Indicadores da tela inicial do painel administrativo. */
public interface DashboardService {

    DashboardResponse gerarResumo();
}
