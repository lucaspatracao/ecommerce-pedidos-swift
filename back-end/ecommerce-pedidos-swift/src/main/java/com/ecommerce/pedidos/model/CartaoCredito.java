package com.ecommerce.pedidos.model;

import java.math.BigDecimal;

public class CartaoCredito extends FormaPagamento {
    private String numeroMascarado;
    private String bandeira;
    private int quantidadeDeParcelas;

    public CartaoCredito(BigDecimal valor, String numeroMascarado, String bandeira, int quantidadeDeParcelas) {
        super(valor);
        setNumeroMascarado(numeroMascarado);
        setBandeira(bandeira);
        setQuantidadeDeParcelas(quantidadeDeParcelas);
    }

    public String getNumeroMascarado() {
        return numeroMascarado;
    }

    public void setNumeroMascarado(String numeroMascarado) {
        if (numeroMascarado == null || numeroMascarado.isBlank()) {
            throw new IllegalArgumentException("Número mascarado é obrigatório");
        }
        this.numeroMascarado = numeroMascarado.trim();
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        if (bandeira == null || bandeira.isBlank()) {
            throw new IllegalArgumentException("Bandeira é obrigatória");
        }
        this.bandeira = bandeira.trim();
    }

    public int getQuantidadeDeParcelas() {
        return quantidadeDeParcelas;
    }

    public void setQuantidadeDeParcelas(int quantidadeDeParcelas) {
        if (quantidadeDeParcelas <= 0 || quantidadeDeParcelas > 12) {
            throw new IllegalArgumentException("Quantidade de parcelas deve estar entre 1 e 12");
        }
        this.quantidadeDeParcelas = quantidadeDeParcelas;
    }

    @Override
    public boolean processar() {
        System.out.println("Processando cartão " + bandeira + " no número " + numeroMascarado + " em " + quantidadeDeParcelas + " parcela(s)");
        setSituacao(SituacaoPagamento.APROVADO);
        return true;
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (cartão " + bandeira + ", parcelas " + quantidadeDeParcelas + ")";
    }
}
