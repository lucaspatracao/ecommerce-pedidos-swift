package com.ecommerce.pedidos.modelo;

import java.util.Arrays;
import java.util.Locale;

public enum SituacaoPedido {
    ABERTO,
    PAGO,
    CANCELADO;

    /**
     * Converte o texto recebido da API ("cancelado", "PAGO"...) na situação.
     * Diferente de {@code valueOf}, ignora maiúsculas/minúsculas e explica
     * quais valores são aceitos quando o texto é inválido.
     */
    public static SituacaoPedido deTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Status do pedido é obrigatório.");
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(situacao -> situacao.name().equals(normalizado))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Status inválido: " + texto + ". Use um destes: " + Arrays.toString(values())));
    }
}
