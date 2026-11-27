package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Cliente;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de {@link Cliente}. Segue as mesmas convenções de
 * {@link ProdutoRepository}: conexão por parâmetro, Optional nas buscas,
 * erro tipado quando nenhuma linha é afetada e filtro {@code ativo = TRUE}
 * por padrão.
 */
public interface ClienteRepository {

    /** Insere quando o id é nulo; caso contrário, atualiza. */
    Cliente salvar(Connection conexao, Cliente cliente);

    /** Atualiza pelo id. Cliente sem id gera erro de validação. */
    Cliente atualizar(Connection conexao, Cliente cliente);

    /**
     * Exclusão definitiva. Cliente com pedidos não pode ser excluído
     * (ON DELETE RESTRICT): lança {@code VIOLACAO_INTEGRIDADE} com mensagem
     * de negócio. Nesses casos, use {@link #inativar}.
     */
    void remover(Connection conexao, Long id);

    void inativar(Connection conexao, Long id);

    void reativar(Connection conexao, Long id);

    Optional<Cliente> buscarPorId(Connection conexao, Long id);

    Optional<Cliente> buscarPorId(Connection conexao, Long id, Visibilidade visibilidade);

    Optional<Cliente> buscarPorCpf(Connection conexao, String cpf);

    Optional<Cliente> buscarPorCpf(Connection conexao, String cpf, Visibilidade visibilidade);

    Optional<Cliente> buscarPorEmail(Connection conexao, String email);

    List<Cliente> listarTodos(Connection conexao, Pagina pagina);

    List<Cliente> listar(Connection conexao, Visibilidade visibilidade, Pagina pagina);

    long contar(Connection conexao);
}
