package com.ecommerce.pedidos.model;

import com.fasterxml.jackson.annotation.JsonValue;

public class Endereco {
    private final String logradouro;
    private final String cidade;
    private final String cep;

    public Endereco(String logradouro, String cidade, String cep) {
        this.logradouro = validar(logradouro, "Logradouro");
        this.cidade = validar(cidade, "Cidade");
        this.cep = validar(cep, "CEP");
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getCep() {
        return cep;
    }

    @JsonValue
    @Override
    public String toString() {
        return logradouro;
    }

    private String validar(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return valor.trim();
    }
}