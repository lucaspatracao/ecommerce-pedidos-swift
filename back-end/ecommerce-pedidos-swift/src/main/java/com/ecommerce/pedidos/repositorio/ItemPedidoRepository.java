package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Itens são gravados em cascata pelo {@code PedidoRepository}; este
 * repositório existe só para consultas sobre itens.
 */
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /** Usado para impedir a exclusão de produto que já foi vendido. */
    boolean existsByProduto(Produto produto);
}
