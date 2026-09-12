package com.ecommerce.pedidos.model;

public class Funcionario extends Pessoa {
    private String matricula;
    private String cargo;

    public Funcionario(String nome, String documento, String matricula, String cargo) {
        super(nome, documento);
        setMatricula(matricula);
        setCargo(cargo);
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória");
        }
        this.matricula = matricula.trim();
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("Cargo é obrigatório");
        }
        this.cargo = cargo.trim();
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
