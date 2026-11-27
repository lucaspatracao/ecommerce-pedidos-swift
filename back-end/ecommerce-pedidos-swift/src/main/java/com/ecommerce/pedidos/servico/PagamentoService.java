package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PagamentoRegistradoResponse;
import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PagamentoResumoResponse;
import java.util.List;

/** Casos de uso de pagamento (Aula 14). */
public interface PagamentoService {

    /** Todos os pagamentos registrados, mais recentes primeiro. */
    List<PagamentoResumoResponse> listar();

    /**
     * Paga um pedido aberto.
     *
     * @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) pedido inexistente
     * @throws com.ecommerce.pedidos.excecao.PagamentoRecusadoException (409) recusa da forma de pagamento ou do gateway
     */
    PagamentoRegistradoResponse registrar(PagamentoRequest request);
}
