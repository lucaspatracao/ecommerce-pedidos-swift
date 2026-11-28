package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corpo de {@code POST /api/pedidos/{numero}/pagamento} (o pedido vem do endereço). Os campos usados dependem do
 * {@code tipo}: CARTAO usa numero/bandeira/parcelas, BOLETO usa
 * vencimento/codigo e PIX usa chave/tipoChave.
 *
 * <p>Aqui só se valida o que é comum a todos os tipos. Os campos de cada
 * forma são validados pelo construtor da subclasse ({@code Pix},
 * {@code Boleto}...), que sabe o que é obrigatório para ela.</p>
 *
 * <p>Valores numéricos e datas podem vir como texto ({@code "99.80"},
 * {@code "3"}, {@code "2026-12-31"}): o Jackson converte.</p>
 */
public record PagamentoRequest(
    @NotBlank(message = "Tipo de pagamento é obrigatório.")
    String tipo,

    @NotNull(message = "Valor do pagamento é obrigatório.")
    @Positive(message = "Valor do pagamento deve ser maior que zero.")
    BigDecimal valor,

    String numero,
    String bandeira,
    Integer parcelas,
    LocalDate vencimento,
    String codigo,
    String chave,
    String tipoChave
) {
}
