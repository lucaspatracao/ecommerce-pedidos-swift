package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso a dados de {@link Pedido}. Os itens são salvos em cascata com o pedido.
 *
 * <h2>O problema N+1 e o JOIN FETCH</h2>
 *
 * <p>Cliente, itens, produto e pagamento são LAZY: o Hibernate só os busca
 * quando o código acessa. Para montar o JSON de uma lista de N pedidos com
 * {@code findAll()}, isso vira 1 consulta para os pedidos + N para os
 * clientes + N para os itens + uma por produto... É o "problema N+1": o
 * número de consultas cresce com o número de linhas.</p>
 *
 * <p>{@code JOIN FETCH} manda o Hibernate trazer tudo numa ÚNICA consulta
 * SQL com JOINs, já preenchendo os objetos relacionados. O teste
 * {@code PedidoRepositoryTest} conta as consultas para provar isso.</p>
 *
 * <p>Os JOINs são {@code LEFT} onde a relação pode não existir (pedido sem
 * pagamento). O Hibernate 6 elimina sozinho as linhas repetidas do pedido
 * que o JOIN com itens produz, sem precisar de {@code DISTINCT}.</p>
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Consulta base reaproveitada: pedido + cliente + itens + produtos + pagamento. */
    String PEDIDO_COMPLETO = """
        select p from Pedido p
          join fetch p.cliente
          left join fetch p.itens i
          left join fetch i.produto
          left join fetch p.formaPagamento
        """;

    Optional<Pedido> findByNumero(String numero);

    /** Um pedido com tudo carregado: 1 consulta em vez de 4 ou mais. Evita N+1. */
    @Query(PEDIDO_COMPLETO + " where p.numero = :numero")
    Optional<Pedido> buscarCompletoPorNumero(@Param("numero") String numero);

    /**
     * Todos os pedidos, mais recentes primeiro, com tudo carregado numa
     * consulta só. Evita N+1 na listagem.
     */
    @Query(PEDIDO_COMPLETO + " order by p.data desc, p.id desc")
    List<Pedido> buscarTodosCompletos();

    /**
     * Passo 1 da listagem paginada: só os ids dos mais recentes.
     *
     * <p>Por que dois passos? Paginar uma consulta com JOIN FETCH de coleção
     * (itens) obrigaria o Hibernate a trazer TODAS as linhas e cortar em
     * memória. Paginando só os ids, o LIMIT vai para o banco.</p>
     */
    @Query("select p.id from Pedido p order by p.data desc, p.id desc")
    List<Long> buscarIdsRecentes(Pageable pagina);

    /** Passo 2: carrega completos (JOIN FETCH) apenas os ids escolhidos. */
    @Query(PEDIDO_COMPLETO + " where p.id in :ids order by p.data desc, p.id desc")
    List<Pedido> buscarCompletosPorIds(@Param("ids") List<Long> ids);

    /** Usado para impedir a exclusão de cliente que já comprou. */
    boolean existsByCliente(Cliente cliente);
}
