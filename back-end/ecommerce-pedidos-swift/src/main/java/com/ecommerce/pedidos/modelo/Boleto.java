package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Pagamento por boleto: exige código de barras e vencimento que não esteja no passado. */
public class Boleto extends FormaPagamento {
    private String codigoDeBarras;
    private LocalDate dataDeVencimento;

    public Boleto(BigDecimal valor, LocalDate dataDeVencimento, String codigoDeBarras) {
        super(valor);
        if (dataDeVencimento == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        if (dataDeVencimento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Vencimento do boleto não pode ser no passado");
        }
        this.dataDeVencimento = dataDeVencimento;
        this.codigoDeBarras = Utils.textoObrigatorio(codigoDeBarras, "Código de barras");
    }

    public String getCodigoDeBarras() {
        return codigoDeBarras;
    }

    public LocalDate getDataDeVencimento() {
        return dataDeVencimento;
    }

    /** Na simulação, o boleto emitido é considerado compensado na hora. */
    @Override
    public boolean processar() {
        aprovar();
        return true;
    }

    @Override
    public String descreverMetodo() {
        return "Código: " + codigoDeBarras;
    }

    @Override
    public String getComprovante() {
        return "COMPROVANTE BOLETO | codigo=" + codigoDeBarras + " | vencimento=" + dataDeVencimento
                + " | valor=R$ " + getValor() + " | data=" + getDataDoPagamento();
    }

    @Override
    public String getDescricao() {
        return "Boleto";
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (boleto vencimento " + dataDeVencimento + ")";
    }
}
