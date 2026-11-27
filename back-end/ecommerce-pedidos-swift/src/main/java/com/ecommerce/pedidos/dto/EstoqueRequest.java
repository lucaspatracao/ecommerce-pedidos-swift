package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Corpo de {@code PATCH /api/produtos/{codigo}/estoque}: só a nova quantidade. */
public record EstoqueRequest(
    @NotNull(message = "Quantidade em estoque é obrigatória.")
    @PositiveOrZero(message = "Estoque do produto não pode ser negativo.")
    Integer quantidade
) {
}
