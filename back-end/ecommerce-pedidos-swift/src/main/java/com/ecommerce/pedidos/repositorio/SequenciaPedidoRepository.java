package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.SequenciaPedido;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Acesso ao contador do número dos pedidos. Só as duas operações necessárias. */
public interface SequenciaPedidoRepository extends Repository<SequenciaPedido, Integer> {

    /**
     * Cria o contador do ano com zero, se ainda não existir. {@code INSERT IGNORE}
     * (MySQL, e H2 em MODE=MySQL) não falha quando outra transação cria a mesma
     * linha ao mesmo tempo: espera e ignora.
     */
    @Modifying
    @Query(value = "INSERT IGNORE INTO sequencia_pedido (ano, ultimo) VALUES (:ano, 0)", nativeQuery = true)
    void criarSeNaoExistir(@Param("ano") int ano);

    /** Lê o contador COM BLOQUEIO (SELECT ... FOR UPDATE) até o fim da transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SequenciaPedido s where s.ano = :ano")
    Optional<SequenciaPedido> travar(@Param("ano") int ano);
}
