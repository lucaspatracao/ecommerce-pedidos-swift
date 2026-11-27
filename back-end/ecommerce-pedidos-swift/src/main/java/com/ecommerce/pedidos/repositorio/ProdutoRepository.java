package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Produto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acesso a dados de {@link Produto}. CRUD herdado de {@code JpaRepository}. */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /**
     * Estoque crítico em JPQL: produtos com saldo abaixo do limite, do menor
     * para o maior. A consulta derivada equivalente teria um nome enorme
     * ({@code findByQuantidadeEmEstoqueLessThanOrderByQuantidadeEmEstoqueAsc}).
     */
    @Query("""
        select p from Produto p
        where p.quantidadeEmEstoque < :limite
        order by p.quantidadeEmEstoque asc, p.nome asc
        """)
    List<Produto> buscarComEstoqueAbaixoDe(@Param("limite") int limite);
}
