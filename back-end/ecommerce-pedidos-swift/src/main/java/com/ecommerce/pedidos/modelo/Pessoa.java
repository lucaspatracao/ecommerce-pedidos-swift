package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;

/**
 * Superclasse abstrata de quem é "pessoa" no sistema (Aula 06).
 *
 * <p>Concentra o que {@link Cliente} e {@link Funcionario} têm em comum
 * (nome e documento). É abstrata porque não existe "uma pessoa genérica":
 * todo objeto concreto é um cliente ou um funcionário.</p>
 */
public abstract class Pessoa {
    private String nome;
    private String documento;

    protected Pessoa(String nome, String documento) {
        alterarNome(nome);
        this.documento = validarDocumento(documento);
    }

    public String getNome() {
        return nome;
    }

    public String getDocumento() {
        return documento;
    }

    /** Só as subclasses podem trocar o nome, sempre passando pela validação. */
    protected final void alterarNome(String nome) {
        this.nome = Utils.textoObrigatorio(nome, "Nome");
    }

    private static String validarDocumento(String documento) {
        String valor = Utils.textoObrigatorio(documento, "Documento");
        if (!valor.matches("\\d+")) {
            throw new IllegalArgumentException("Documento deve conter apenas dígitos.");
        }
        return valor;
    }

    public String getResumo() {
        return nome + " (" + documento + ")";
    }

    /** Cada tipo de pessoa decide como se identifica (polimorfismo). */
    public abstract String getIdentificacao();
}
