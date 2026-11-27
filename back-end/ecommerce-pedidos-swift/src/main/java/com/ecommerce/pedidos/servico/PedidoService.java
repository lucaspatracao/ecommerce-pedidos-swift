package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;

/** Casos de uso de pedido (Aula 14). */
public interface PedidoService {

    /** Todos os pedidos, mais recentes primeiro. */
    default List<PedidoResponse> listarTodos() {
        return listar(null, Pageable.unpaged());
    }

    /**
     * Pedidos mais recentes primeiro.
     * @param situacao ABERTO, PAGO ou CANCELADO (sem diferenciar maiúsculas); {@code null} = todas
     */
    List<PedidoResponse> listar(String situacao, Pageable pagina);

    /** Pedidos de um cliente, inclusive inativo (histórico). 404 se o CPF não existir. */
    List<PedidoResponse> listarPorCliente(String cpf, Pageable pagina);

    /** Os {@code limite} pedidos mais recentes. */
    List<PedidoResponse> listarRecentes(int limite);

    /** @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) */
    PedidoResponse buscarPorNumero(String numero);

    /**
     * Cria o pedido e baixa o estoque de todos os itens numa única transação:
     * se qualquer item falhar, nenhum estoque é alterado (rollback).
     */
    PedidoResponse criar(PedidoRequest request);

    /** Aceita "CANCELADO" (o mesmo que {@link #cancelar}) ou "ABERTO" (sem mudança). */
    PedidoResponse atualizarStatus(String numero, String status);

    /** Cancela: devolve o estoque e estorna o pagamento. Cancelar de novo → 409. */
    PedidoResponse cancelar(String numero);

    /** Remove um item de pedido ABERTO e devolve a quantidade ao estoque. */
    PedidoResponse removerItem(String numero, Long itemId);

    /**
     * Exclui só pedido ABERTO (ex.: carrinho abandonado), devolvendo o estoque.
     * Pago ou cancelado faz parte do histórico: 409 (pago deve ser cancelado).
     */
    void excluir(String numero);

    long contar();

    BigDecimal calcularReceitaTotal();
}
