package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

/** Corpo de PUT {@code /api/pedidos/{numero}/status}. */
public record AtualizarStatusRequest(
    @NotBlank(message = "Status do pedido é obrigatório.")
    String status
) {
}
