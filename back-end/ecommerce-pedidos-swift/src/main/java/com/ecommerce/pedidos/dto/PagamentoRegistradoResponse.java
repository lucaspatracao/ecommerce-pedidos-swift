package com.ecommerce.pedidos.dto;

import java.util.Map;

/** Resposta de {@code POST /api/pagamentos}. */
public record PagamentoRegistradoResponse(String mensagem, Map<String, Object> pagamento) {
}
