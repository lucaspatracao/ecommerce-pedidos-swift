package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso a dados de {@link Cliente} (Aulas 12–13).
 *
 * <p>Só a interface é escrita: o Spring Data gera a implementação em tempo
 * de execução. {@code JpaRepository} já traz o CRUD (save, findById,
 * findAll, delete, count...).</p>
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Consulta JPQL escrita à mão com {@code @Query}.
     *
     * <p>JPQL fala de CLASSES e ATRIBUTOS Java, não de tabelas e colunas:
     * {@code Cliente c} é a entidade e {@code c.documento} é o atributo
     * herdado de {@code Pessoa} (no banco, a coluna {@code cpf}). Uma consulta
     * derivada teria de se chamar {@code findByDocumento}; com {@code @Query}
     * o nome do método pode dizer o que ele faz.</p>
     *
     * <p>{@code @Param("cpf")} liga o parâmetro Java ao {@code :cpf} da
     * consulta, com nome explícito (mesmo motivo do bug do -parameters).</p>
     */
    @Query("select c from Cliente c where c.documento = :cpf")
    Optional<Cliente> buscarPorCpf(@Param("cpf") String cpf);

    /** Consulta derivada: o Spring monta o SQL a partir do nome do método. */
    boolean existsByDocumento(String cpf);
}
