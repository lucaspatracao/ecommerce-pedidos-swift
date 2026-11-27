package com.ecommerce.pedidos.dto;

/** Corpo de POST/PUT {@code /api/clientes}. O endereço chega como texto livre. */
public record ClienteRequest(
    String nome,
    String cpf,
    String email,
    String telefone,
    String endereco
) {
}
