package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Um item dentro de {@link PedidoRequest}: código do produto e quantidade. */
public record ItemPedidoRequest(
    @NotBlank(message = "Código do produto do item é obrigatório.")
    String codigo,

    @NotNull(message = "Quantidade do item é obrigatória.")
    @Positive(message = "Quantidade do item deve ser maior que zero.")
    Integer quantidade
) {
}
