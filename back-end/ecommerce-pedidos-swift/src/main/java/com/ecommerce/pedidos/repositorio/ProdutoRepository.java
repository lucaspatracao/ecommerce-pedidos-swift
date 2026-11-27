package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Produto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Produto}. CRUD herdado de {@code JpaRepository}. */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /** Produtos com estoque abaixo do limite, do menor para o maior saldo. */
    List<Produto> findByQuantidadeEmEstoqueLessThanOrderByQuantidadeEmEstoqueAsc(int limite);
}
