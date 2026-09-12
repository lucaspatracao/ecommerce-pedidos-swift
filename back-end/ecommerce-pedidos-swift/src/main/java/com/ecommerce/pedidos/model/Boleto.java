package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Boleto extends FormaPagamento {
    private String codigoDeBarras;
    private LocalDate dataDeVencimento;

    public Boleto(BigDecimal valor, LocalDate dataDeVencimento, String codigoDeBarras) {
        super(valor);
        setDataDeVencimento(dataDeVencimento);
        setCodigoDeBarras(codigoDeBarras);
    }

    public String getCodigoDeBarras() {
        return codigoDeBarras;
    }

    public void setCodigoDeBarras(String codigoDeBarras) {
        if (codigoDeBarras == null || codigoDeBarras.isBlank()) {
            throw new IllegalArgumentException("Código de barras é obrigatório");
        }
        this.codigoDeBarras = codigoDeBarras.trim();
    }

    public LocalDate getDataDeVencimento() {
        return dataDeVencimento;
    }

    public void setDataDeVencimento(LocalDate dataDeVencimento) {
        if (dataDeVencimento == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        if (dataDeVencimento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Vencimento do boleto não pode ser no passado");
        }
        this.dataDeVencimento = dataDeVencimento;
    }

    @Override
    public boolean processar() {
        System.out.println("Processando boleto com vencimento " + dataDeVencimento + " e código " + codigoDeBarras);
        setSituacao(SituacaoPagamento.APROVADO);
        return true;
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (boleto vencimento " + dataDeVencimento + ")";
    }
}
