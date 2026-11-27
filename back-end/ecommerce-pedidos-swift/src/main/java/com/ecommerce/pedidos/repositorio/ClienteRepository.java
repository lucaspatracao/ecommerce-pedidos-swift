package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a dados de {@link Cliente} (Aulas 12–13).
 *
 * <p>Só a interface é escrita: o Spring Data gera a implementação em tempo
 * de execução. {@code JpaRepository} já traz o CRUD (save, findById,
 * findAll, delete, count...) e os métodos abaixo são "consultas derivadas":
 * o Spring lê o NOME do método e monta o SQL.</p>
 *
 * <p>O CPF está no campo herdado {@code documento} (coluna {@code cpf}),
 * por isso os métodos dizem "Documento".</p>
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** SELECT ... FROM cliente WHERE cpf = ? */
    Optional<Cliente> findByDocumento(String cpf);

    boolean existsByDocumento(String cpf);
}
