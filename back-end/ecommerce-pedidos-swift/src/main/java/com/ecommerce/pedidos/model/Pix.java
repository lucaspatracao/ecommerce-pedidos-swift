package com.ecommerce.pedidos.model;

import java.math.BigDecimal;

public class Pix extends FormaPagamento {
    private String chave;
    private String tipoDaChave;

    public Pix(BigDecimal valor, String chave, String tipoDaChave) {
        super(valor);
        setChave(chave);
        setTipoDaChave(tipoDaChave);
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Chave Pix é obrigatória");
        }
        this.chave = chave.trim();
    }

    public String getTipoDaChave() {
        return tipoDaChave;
    }

    public void setTipoDaChave(String tipoDaChave) {
        if (tipoDaChave == null || tipoDaChave.isBlank()) {
            throw new IllegalArgumentException("Tipo da chave é obrigatório");
        }
        this.tipoDaChave = tipoDaChave.trim().toUpperCase();
    }

    @Override
    public boolean processar() {
        System.out.println("Processando Pix para a chave " + chave + " (tipo " + tipoDaChave + ")");
        setSituacao(SituacaoPagamento.APROVADO);
        return true;
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (chave " + chave + ", tipo " + tipoDaChave + ")";
    }
}
