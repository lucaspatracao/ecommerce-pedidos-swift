package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.FormaPagamento;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Monta o JSON completo de um pagamento: campos comuns + campos da subclasse.
 *
 * <p>É um {@code Map} (e não um record) porque cada forma de pagamento tem
 * campos diferentes. Os campos próprios vêm de
 * {@link FormaPagamento#dadosEspecificos()}, que é polimórfico, então nenhum
 * {@code instanceof} é necessário.</p>
 */
public final class PagamentoDetalhe {

    private PagamentoDetalhe() {
    }

    /** Devolve {@code null} para pedido ainda sem pagamento (mesmo JSON de antes). */
    public static Map<String, Object> de(FormaPagamento pagamento) {
        if (pagamento == null) {
            return null;
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("valor", pagamento.getValor());
        json.put("dataDoPagamento", pagamento.getDataDoPagamento());
        json.put("situacao", pagamento.getSituacao());
        json.putAll(pagamento.dadosEspecificos());
        json.put("descricao", pagamento.getDescricao());
        json.put("comprovante", pagamento.getComprovante());
        json.put("resumo", pagamento.getResumo());
        return json;
    }
}
