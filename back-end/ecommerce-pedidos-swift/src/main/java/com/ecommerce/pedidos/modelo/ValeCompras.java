package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Quarta forma de pagamento. Existe para mostrar o princípio aberto/fechado:
 * foi adicionada sem alterar {@code Pedido} nem o serviço de pagamento.
 */
@Entity
public class ValeCompras extends FormaPagamento {
    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String emissor;

    protected ValeCompras() {
        // exigido pelo JPA
    }

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
    public Map<String, Object> dadosEspecificos() {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("codigo", codigo);
        dados.put("emissor", emissor);
        return dados;
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
