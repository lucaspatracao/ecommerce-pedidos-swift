package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import java.math.BigDecimal;
import java.util.List;

/** Casos de uso de pedido (Aula 14). */
public interface PedidoService {

    /** Todos os pedidos, mais recentes primeiro. */
    List<PedidoResponse> listarTodos();

    /** Os {@code limite} pedidos mais recentes. */
    List<PedidoResponse> listarRecentes(int limite);

    /** @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) */
    PedidoResponse buscarPorNumero(String numero);

    /**
     * Cria o pedido e baixa o estoque de todos os itens numa única transação:
     * se qualquer item falhar, nenhum estoque é alterado (rollback).
     */
    PedidoResponse criar(PedidoRequest request);

    /** Aceita "CANCELADO" (devolve estoque e estorna pagamento) ou "ABERTO" (sem mudança). */
    PedidoResponse atualizarStatus(String numero, String status);

    /** Exclui pedido não pago; se estiver aberto, devolve o estoque antes. */
    void excluir(String numero);

    long contar();

    BigDecimal calcularReceitaTotal();
}
