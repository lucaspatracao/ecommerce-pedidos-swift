package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pagamento instantâneo por chave Pix. */
@Entity
public class Pix extends FormaPagamento {
    @Column(nullable = false, length = 140)
    private String chave;

    @Column(nullable = false, length = 20)
    private String tipoDaChave;

    protected Pix() {
        // exigido pelo JPA
    }

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
    public Map<String, Object> dadosEspecificos() {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("chave", chave);
        dados.put("tipoDaChave", tipoDaChave);
        return dados;
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
