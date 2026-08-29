package com.ecommerce.pedidos.modelo;

/**
 * Representa os dados cadastrais do cliente responsavel pela compra no
 * e-commerce.
 */
public class Cliente {
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;

    public Cliente(String nome, String cpf, String email, String telefone, String endereco) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    /**
     * Identificacao textual do cliente, usada em recibos e logs de pedido.
     */
    public String getIdentificacao() {
        return String.format("%s (%s)", nome, cpf);
    }

    @Override
    public String toString() {
        return String.format("Cliente: %s | CPF: %s | E-mail: %s", nome, cpf, email);
    }
}