package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;

/** Pagamento instantâneo por chave Pix. */
public class Pix extends FormaPagamento {
    private String chave;
    private String tipoDaChave;

    public Pix(BigDecimal valor, String chave, String tipoDaChave) {
        super(valor);
        this.chave = Utils.textoObrigatorio(chave, "Chave Pix");
        this.tipoDaChave = Utils.textoObrigatorio(tipoDaChave, "Tipo da chave").toUpperCase();
    }

    public String getChave() {
        return chave;
    }

    public String getTipoDaChave() {
        return tipoDaChave;
    }

    @Override
    public boolean processar() {
        aprovar();
        return true;
    }

    @Override
    public String descreverMetodo() {
        return "Chave: " + chave;
    }

    @Override
    public String getComprovante() {
        return "COMPROVANTE PIX | chave=" + chave + " | tipo=" + tipoDaChave + " | valor=R$ " + getValor()
                + " | data=" + getDataDoPagamento();
    }

    @Override
    public String getDescricao() {
        return "Pix";
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (chave " + chave + ", tipo " + tipoDaChave + ")";
    }
}
