package com.ecommerce.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Corpo de POST {@code /api/pedidos}. */
public record PedidoRequest(
    @NotBlank(message = "CPF do cliente é obrigatório.")
    String clienteCpf,

    // @Valid faz a validação "descer" para cada ItemPedidoRequest da lista.
    @NotEmpty(message = "Pedido deve conter pelo menos um item.")
    List<@Valid ItemPedidoRequest> itens
) {
}
