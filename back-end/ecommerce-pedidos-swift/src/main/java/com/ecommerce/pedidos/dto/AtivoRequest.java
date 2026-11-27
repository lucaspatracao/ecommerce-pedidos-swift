package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo de {@code PATCH /api/clientes/{cpf}} e {@code PATCH /api/produtos/{codigo}}:
 * {@code { "ativo": false }} inativa (soft delete) e {@code { "ativo": true }} reativa.
 */
public record AtivoRequest(
    @NotNull(message = "Informe se o registro fica ativo (true) ou inativo (false).")
    Boolean ativo
) {
}
