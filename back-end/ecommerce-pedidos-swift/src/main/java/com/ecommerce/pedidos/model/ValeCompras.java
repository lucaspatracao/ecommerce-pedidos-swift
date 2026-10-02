package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ValeCompras extends FormaPagamento {
    private String codigo;
    private String emissor;

    public ValeCompras(BigDecimal valor, String codigo, String emissor) {
        super(valor);
        setCodigo(codigo);
        setEmissor(emissor);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do vale-compras é obrigatório");
        }
        this.codigo = codigo.trim();
    }

    public String getEmissor() {
        return emissor;
    }

    public void setEmissor(String emissor) {
        if (emissor == null || emissor.isBlank()) {
            throw new IllegalArgumentException("Emissor do vale-compras é obrigatório");
        }
        this.emissor = emissor.trim();
    }

    @Override
    public boolean processar() {
        setDataDoPagamento(LocalDateTime.now());
        setSituacao(SituacaoPagamento.APROVADO);
        return true;
    }

    @Override
    public String getComprovante() {
        return "COMPROVANTE VALE-COMPRAS | codigo=" + codigo + " | emissor=" + emissor
                + " | valor=R$ " + getValor() + " | data=" + getDataDoPagamento();
    }

    @Override
    public String getDescricao() {
        return "ValeCompras";
    }

    @Override
    public boolean permiteParcelamento() {
        return false;
    }
}
