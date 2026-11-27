package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.FormaPagamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório da classe ABSTRATA {@link FormaPagamento}. Por causa da
 * herança JOINED, uma consulta aqui traz objetos das subclasses concretas
 * (Pix, Boleto, CartaoCredito, ValeCompras): polimorfismo também no banco.
 */
public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {

    List<FormaPagamento> findAllByOrderByDataDoPagamentoDesc();
}
