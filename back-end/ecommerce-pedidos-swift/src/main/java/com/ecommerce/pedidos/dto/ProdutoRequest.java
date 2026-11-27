package com.ecommerce.pedidos.dto;

/** Corpo de POST/PUT {@code /api/produtos}. */
public record ProdutoRequest(
    String codigo,
    String nome,
    String descricao,
    String preco,
    Integer estoque,
    Boolean disponivel
) {
}
