package com.ecommerce.pedidos.model;

/**
 * Representa os dados cadastrais do cliente responsavel pela compra no
 * e-commerce.
 */
public class Cliente extends Pessoa {
    private String email;
    private String telefone;
    private Endereco endereco;

    public Cliente(String nome, String cpf, String email, String telefone, String endereco) {
        this(nome, cpf, email, telefone, endereco == null || endereco.isBlank()
                ? null
                : new Endereco(endereco, "Não informado", "Não informado"));
    }

    public Cliente(String nome, String cpf, String email, String telefone, Endereco endereco) {
        super(nome, cpf);
        setEmail(email);
        setTelefone(telefone);
        setEndereco(endereco);
    }

    public String getCpf() {
        return getDocumento();
    }

    public void setCpf(String cpf) {
        setDocumento(cpf);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("E-mail não pode ser nulo.");
        }
        String emailValidado = email.trim();
        if (emailValidado.isEmpty()) {
            throw new IllegalArgumentException("E-mail não pode estar vazio.");
        }
        if (!emailValidado.contains("@")) {
            throw new IllegalArgumentException("E-mail deve conter '@'.");
        }
        if (!emailValidado.contains(".")) {
            throw new IllegalArgumentException("E-mail deve conter domínio válido.");
        }
        this.email = emailValidado;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        if (telefone == null) {
            throw new IllegalArgumentException("Telefone não pode ser nulo.");
        }
        String telefoneValidado = telefone.trim();
        if (telefoneValidado.isEmpty()) {
            throw new IllegalArgumentException("Telefone não pode estar vazio.");
        }
        if (!telefoneValidado.matches("\\d+")) {
            throw new IllegalArgumentException("Telefone deve conter apenas dígitos.");
        }
        this.telefone = telefoneValidado;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    /**
     * Identificacao textual do cliente, usada em recibos e logs de pedido.
     */
    @Override
    public String getIdentificacao() {
        return String.format("%s (%s)", getNome(), getDocumento());
    }

    @Override
    public String toString() {
        return String.format("Cliente: %s | CPF: %s | E-mail: %s", getNome(), getDocumento(), email);
    }
}