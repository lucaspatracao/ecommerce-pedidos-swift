package com.ecommerce.pedidos.model;

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

    @Override
    public String toString() {
        return String.format("%s, %s - CEP: %s", logradouro, cidade, cep);
    }

    private String validar(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return valor.trim();
    }
}