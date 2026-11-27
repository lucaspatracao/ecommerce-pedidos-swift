package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Produto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acesso a dados de {@link Produto}. CRUD herdado de {@code JpaRepository}. */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Listagem com soft delete e busca por nome. {@code ativo} nulo = todos;
     * {@code nome} nulo = sem filtro. O {@code Pageable} vira LIMIT/OFFSET no banco.
     */
    @Query("""
        select p from Produto p
        where (:ativo is null or p.ativo = :ativo)
          and (:nome is null or lower(p.nome) like lower(concat('%', :nome, '%')))
        order by p.nome, p.id
        """)
    List<Produto> listar(@Param("ativo") Boolean ativo, @Param("nome") String nome, Pageable pagina);

    Optional<Produto> findByCodigoAndAtivo(String codigo, boolean ativo);

    /**
     * Leitura COM BLOQUEIO (SELECT ... FOR UPDATE) usada na venda: uma segunda
     * venda do mesmo produto espera esta transação terminar e enxerga o estoque
     * real, em vez de um valor lido antes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.codigo = :codigo")
    Optional<Produto> buscarPorCodigoParaVenda(@Param("codigo") String codigo);

    long countByAtivoTrue();

    Optional<Produto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /**
     * Estoque crítico em JPQL: produtos com saldo abaixo do limite, do menor
     * para o maior. A consulta derivada equivalente teria um nome enorme
     * ({@code findByQuantidadeEmEstoqueLessThanOrderByQuantidadeEmEstoqueAsc}).
     */
    @Query("""
        select p from Produto p
        where p.ativo = true and p.quantidadeEmEstoque < :limite
        order by p.quantidadeEmEstoque asc, p.nome asc
        """)
    List<Produto> buscarComEstoqueAbaixoDe(@Param("limite") int limite);
}
