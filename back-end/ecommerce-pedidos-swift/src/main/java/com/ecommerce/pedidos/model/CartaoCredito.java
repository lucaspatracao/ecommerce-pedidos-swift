package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartaoCredito extends FormaPagamento {
    private String numeroMascarado;
    private String bandeira;
    private int quantidadeDeParcelas;
    private BigDecimal limiteDisponivel;

    public CartaoCredito(BigDecimal valor, String numeroMascarado, String bandeira, int quantidadeDeParcelas) {
        super(valor);
        setNumeroMascarado(numeroMascarado);
        setBandeira(bandeira);
        setQuantidadeDeParcelas(quantidadeDeParcelas);
        this.limiteDisponivel = new BigDecimal("5000.00");
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

    public BigDecimal getLimiteDisponivel() {
        return limiteDisponivel;
    }

    public void setLimiteDisponivel(BigDecimal limiteDisponivel) {
        if (limiteDisponivel == null || limiteDisponivel.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Limite disponível inválido");
        }
        this.limiteDisponivel = limiteDisponivel;
    }

    @Override
    public boolean processar() {
        if (getValor().compareTo(limiteDisponivel) > 0) {
            setSituacao(SituacaoPagamento.RECUSADO);
            return false;
        }
        setDataDoPagamento(LocalDateTime.now());
        setSituacao(SituacaoPagamento.APROVADO);
        limiteDisponivel = limiteDisponivel.subtract(getValor());
        return true;
    }

    @Override
    public boolean permiteParcelamento() {
        return true;
    }

    @Override
    public String getComprovante() {
        return "COMPROVANTE CARTAO | bandeira=" + bandeira + " | numero=" + numeroMascarado
                + " | parcelas=" + quantidadeDeParcelas + " | valor=R$ " + getValor() + " | data=" + getDataDoPagamento();
    }

    @Override
    public String getDescricao() {
        return "CartaoCredito";
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (cartão " + bandeira + ", parcelas " + quantidadeDeParcelas + ")";
    }
}
