package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.Endereco;

public record EnderecoResponse(String logradouro, String cidade, String cep) {

    /** Devolve {@code null} quando o cliente não tem endereço (mesmo JSON de antes). */
    public static EnderecoResponse de(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoResponse(endereco.getLogradouro(), endereco.getCidade(), endereco.getCep());
    }
}
