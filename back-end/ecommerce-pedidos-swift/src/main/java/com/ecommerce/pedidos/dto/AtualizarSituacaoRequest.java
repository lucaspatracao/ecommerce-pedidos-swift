package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

/** Corpo de {@code PATCH /api/pedidos/{numero}}: {@code { "situacao": "CANCELADO" }}. */
public record AtualizarSituacaoRequest(
    @NotBlank(message = "Situação do pedido é obrigatória.")
    String situacao
) {
}
