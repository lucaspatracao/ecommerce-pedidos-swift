package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.modelo.FormaPagamento;

/**
 * Porta para o sistema EXTERNO que autoriza cobranças (operadora de cartão,
 * banco, PSP do Pix...).
 *
 * <p>É uma interface justamente por ser externa: em produção seria uma
 * chamada HTTP; em desenvolvimento usamos {@link GatewayPagamentoSimulado};
 * nos testes ela é substituída por um mock do Mockito para simular
 * aprovação ou recusa sem depender de rede (Aula 11).</p>
 */
public interface GatewayPagamento {

    /** @return {@code true} se a cobrança foi autorizada pelo sistema externo */
    boolean autorizar(FormaPagamento pagamento);
}
