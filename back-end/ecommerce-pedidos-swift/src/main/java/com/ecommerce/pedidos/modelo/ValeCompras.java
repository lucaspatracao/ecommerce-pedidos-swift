package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;

/**
 * Quarta forma de pagamento. Existe para mostrar o princípio aberto/fechado:
 * foi adicionada sem alterar {@code Pedido} nem o serviço de pagamento.
 */
public class ValeCompras extends FormaPagamento {
    private String codigo;
    private String emissor;

    public ValeCompras(BigDecimal valor, String codigo, String emissor) {
        super(valor);
        this.codigo = Utils.textoObrigatorio(codigo, "Código do vale-compras");
        this.emissor = Utils.textoObrigatorio(emissor, "Emissor do vale-compras");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEmissor() {
        return emissor;
    }

    @Override
    public boolean processar() {
        aprovar();
        return true;
    }

    @Override
    public String descreverMetodo() {
        return "Vale " + emissor + " (" + codigo + ")";
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
}
