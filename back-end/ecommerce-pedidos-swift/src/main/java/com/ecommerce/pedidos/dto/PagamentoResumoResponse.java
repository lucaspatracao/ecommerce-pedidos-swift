package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.FormaPagamento;
import java.math.BigDecimal;

/** Linha da listagem {@code GET /api/pagamentos}. */
public record PagamentoResumoResponse(
    String tipo,
    BigDecimal valor,
    String status,
    String ultimaTransacao,
    String metodo
) {

    public static PagamentoResumoResponse de(FormaPagamento pagamento) {
        return new PagamentoResumoResponse(
            pagamento.getDescricao(),
            pagamento.getValor(),
            pagamento.getSituacao().name(),
            pagamento.getDataDoPagamento().toString(),
            pagamento.descreverMetodo()
        );
    }
}
