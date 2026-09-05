package com.ecommerce.pedidos.model;

import java.util.Objects;

public abstract class Pessoa {
    private String nome;
    private String documento;

    protected Pessoa(String nome, String documento) {
        setNome(nome);
        setDocumento(documento);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio.");
        }
        this.nome = nome.trim();
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        String documentoValidado = validarTextoObrigatorio(documento, "Documento");
        if (!documentoValidado.matches("\\d+")) {
            throw new IllegalArgumentException("Documento deve conter apenas dígitos.");
        }
        this.documento = documentoValidado;
    }

    protected String validarTextoObrigatorio(String valor, String nomeCampo) {
        Objects.requireNonNull(valor, nomeCampo + " não pode ser nulo.");
        String valorNormalizado = valor.trim();
        if (valorNormalizado.isBlank()) {
            throw new IllegalArgumentException(nomeCampo + " não pode estar em branco.");
        }
        return valorNormalizado;
    }

    public abstract String getIdentificacao();
}
