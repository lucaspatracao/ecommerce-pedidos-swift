package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public abstract class FormaPagamento {
    private BigDecimal valor;
    private LocalDateTime dataDoPagamento;
    private SituacaoPagamento situacao;

    protected FormaPagamento(BigDecimal valor) {
        setValor(valor);
        this.dataDoPagamento = LocalDateTime.now();
        this.situacao = SituacaoPagamento.PENDENTE;
    }

    protected FormaPagamento(double valor) {
        this(BigDecimal.valueOf(valor));
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor da forma de pagamento não pode ser nulo.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da forma de pagamento deve ser maior que zero.");
        }
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDateTime getDataDoPagamento() {
        return dataDoPagamento;
    }

    public void setDataDoPagamento(LocalDateTime dataDoPagamento) {
        if (dataDoPagamento == null) {
            throw new IllegalArgumentException("Data do pagamento não pode ser nula.");
        }
        this.dataDoPagamento = dataDoPagamento;
    }

    public SituacaoPagamento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoPagamento situacao) {
        if (situacao == null) {
            throw new IllegalArgumentException("Situação do pagamento não pode ser nula.");
        }
        this.situacao = situacao;
    }

    public abstract boolean processar();

    public String getResumo() {
        return String.format("%s no valor de R$ %s", getClass().getSimpleName(), valor);
    }
}
