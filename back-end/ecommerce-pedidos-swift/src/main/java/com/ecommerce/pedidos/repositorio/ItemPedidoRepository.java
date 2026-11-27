package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Produto;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Itens são gravados em cascata pelo {@code PedidoRepository}; este
 * repositório existe só para consultas sobre itens.
 */
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /** Usado para impedir a exclusão de produto que já foi vendido. */
    boolean existsByProduto(Produto produto);

    /**
     * Receita total calculada NO BANCO com uma agregação JPQL (SUM), em vez
     * de carregar todos os pedidos e somar em Java. {@code coalesce} devolve
     * 0 quando ainda não há itens (SUM de nada é NULL em SQL).
     */
    @Query("select coalesce(sum(i.precoPraticado * i.quantidade), 0) from ItemPedido i")
    BigDecimal calcularReceitaTotal();
}
