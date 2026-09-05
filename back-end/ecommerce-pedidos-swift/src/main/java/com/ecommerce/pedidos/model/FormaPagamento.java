package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class FormaPagamento {
    private BigDecimal valor;

    protected FormaPagamento(BigDecimal valor) {
        setValor(valor);
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

    public abstract boolean processar();
}
