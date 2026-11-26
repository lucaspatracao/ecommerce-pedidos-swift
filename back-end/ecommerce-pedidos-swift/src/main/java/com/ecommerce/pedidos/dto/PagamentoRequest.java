package com.ecommerce.pedidos.dto;

/**
 * Corpo de {@code POST /api/pagamentos}. Os campos usados dependem do
 * {@code tipo}: CARTAO usa numero/bandeira/parcelas, BOLETO usa
 * vencimento/codigo e PIX usa chave/tipoChave.
 */
public record PagamentoRequest(
    String tipo,
    String valor,
    String numero,
    String bandeira,
    String parcelas,
    String vencimento,
    String codigo,
    String chave,
    String tipoChave,
    String pedidoNumero
) {
}
