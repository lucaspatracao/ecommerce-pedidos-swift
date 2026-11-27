package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pagamento por boleto: exige código de barras e vencimento que não esteja no passado. */
@Entity
public class Boleto extends FormaPagamento {
    @Column(nullable = false, length = 60)
    private String codigoDeBarras;

    @Column(nullable = false)
    private LocalDate dataDeVencimento;

    protected Boleto() {
        // exigido pelo JPA (lê do banco sem revalidar o vencimento)
    }

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
    public Map<String, Object> dadosEspecificos() {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("codigoDeBarras", codigoDeBarras);
        dados.put("dataDeVencimento", dataDeVencimento);
        return dados;
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
