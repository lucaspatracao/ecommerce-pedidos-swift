package com.ecommerce.pedidos.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contador do número dos pedidos de um ano (tabela {@code sequencia_pedido}).
 *
 * <p>É lido com bloqueio de linha na mesma transação que grava o pedido: dois
 * pedidos simultâneos nunca recebem o mesmo valor, e se a transação falhar o
 * incremento é desfeito junto (sem buracos na numeração).</p>
 */
@Entity
@Table(name = "sequencia_pedido")
public class SequenciaPedido {

    @Id
    private Integer ano;

    @Column(nullable = false)
    private int ultimo;

    protected SequenciaPedido() {
        // exigido pelo JPA
    }

    public Integer getAno() {
        return ano;
    }

    /** Avança o contador e devolve o novo valor (1, 2, 3...). */
    public int proximo() {
        ultimo++;
        return ultimo;
    }
}
