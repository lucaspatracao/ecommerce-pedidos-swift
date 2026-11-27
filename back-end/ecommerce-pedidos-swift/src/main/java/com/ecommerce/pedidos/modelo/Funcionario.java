package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;

/**
 * Segunda subclasse de {@link Pessoa} (Aula 06). Mostra que a mesma
 * superclasse serve a papéis diferentes, cada um com seus atributos e sua
 * própria forma de se identificar.
 */
public class Funcionario extends Pessoa {
    private final String matricula;
    private String cargo;

    public Funcionario(String nome, String documento, String matricula, String cargo) {
        super(nome, documento);
        this.matricula = Utils.textoObrigatorio(matricula, "Matrícula");
        promoverPara(cargo);
    }

    public String getMatricula() {
        return matricula;
    }

    public String getCargo() {
        return cargo;
    }

    /** Troca o cargo do funcionário (a matrícula nunca muda). */
    public void promoverPara(String novoCargo) {
        this.cargo = Utils.textoObrigatorio(novoCargo, "Cargo");
    }

    @Override
    public String getIdentificacao() {
        return getNome() + " - matrícula " + matricula;
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " - " + cargo;
    }
}
