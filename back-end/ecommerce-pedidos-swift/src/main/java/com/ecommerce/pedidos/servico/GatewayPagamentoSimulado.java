package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.modelo.FormaPagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação de desenvolvimento do {@link GatewayPagamento}: autoriza
 * tudo e só registra no log. As regras de aprovação do próprio sistema
 * (limite do cartão, vencimento do boleto) continuam nas classes de domínio.
 */
@Component
public class GatewayPagamentoSimulado implements GatewayPagamento {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayPagamentoSimulado.class);

    @Override
    public boolean autorizar(FormaPagamento pagamento) {
        LOG.info("Gateway simulado autorizou {} de R$ {}", pagamento.getDescricao(), pagamento.getValor());
        return true;
    }
}
