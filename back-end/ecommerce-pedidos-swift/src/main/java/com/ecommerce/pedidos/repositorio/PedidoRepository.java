package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Pedido}. Os itens são salvos em cascata com o pedido. */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumero(String numero);

    /** Usado para impedir a exclusão de cliente que já comprou. */
    boolean existsByCliente(Cliente cliente);

    /** Mais recentes primeiro; o id desempata pedidos do mesmo dia. */
    List<Pedido> findAllByOrderByDataDescIdDesc();

    /** Mesma ordenação, limitada pelo {@code Pageable} (ex.: os 5 últimos). */
    List<Pedido> findAllByOrderByDataDescIdDesc(Pageable pagina);
}
