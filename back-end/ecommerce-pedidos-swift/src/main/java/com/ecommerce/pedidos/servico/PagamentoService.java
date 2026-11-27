package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PagamentoResumoResponse;
import java.util.List;
import java.util.Map;

/**
 * Casos de uso de pagamento (Aula 14). O pagamento é sub-recurso do pedido
 * (Aula 15): os métodos recebem o número do pedido que vem do endereço.
 * O corpo devolvido é o de {@link com.ecommerce.pedidos.dto.PagamentoDetalhe}.
 */
public interface PagamentoService {

    /** Todos os pagamentos registrados, mais recentes primeiro. */
    List<PagamentoResumoResponse> listar();

    /**
     * Paga um pedido aberto.
     *
     * @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) pedido inexistente
     * @throws com.ecommerce.pedidos.excecao.PagamentoRecusadoException (409) recusa da forma de pagamento ou do gateway
     */
    Map<String, Object> registrar(String numeroPedido, PagamentoRequest request);

    /** @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) pedido inexistente ou sem pagamento */
    Map<String, Object> buscarPorPedido(String numeroPedido);
}
