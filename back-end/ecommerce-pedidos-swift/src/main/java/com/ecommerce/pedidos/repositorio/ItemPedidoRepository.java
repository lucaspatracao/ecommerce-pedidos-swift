package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.ItemPedido;
import java.sql.Connection;
import java.util.List;

/**
 * Contrato de persistência de {@link ItemPedido}. Normalmente é usado através
 * do fluxo de pedido (o item não existe sem o pedido).
 */
public interface ItemPedidoRepository {

    /** Grava o item com o preço praticado na venda e preenche o id gerado. */
    ItemPedido inserir(Connection conexao, Long pedidoId, ItemPedido item);

    /** Itens do pedido, incluindo produtos já inativados. */
    List<ItemPedido> listarPorPedido(Connection conexao, Long pedidoId);

    void remover(Connection conexao, Long itemId);

    long contarPorPedido(Connection conexao, Long pedidoId);
}
