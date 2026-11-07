package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Endereco;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementação MySQL de {@link ClienteRepository}. Assim como em produto,
 * o filtro de soft delete é aplicado em um único método ({@link #select}).
 */
public class ClienteRepositoryMySql extends RepositorioJdbcBase implements ClienteRepository {
    private static final String ENTIDADE = "Cliente";
    private static final String DUPLICADO = "Já existe um cliente com este CPF ou e-mail.";
    private static final String COM_PEDIDOS = "Este cliente possui pedidos e não pode ser excluído. "
        + "Inative o cadastro para preservar o histórico de compras.";

    @Override
    public Cliente salvar(Connection conexao, Cliente cliente) {
        if (cliente == null) {
            throw RepositorioException.validacao("Cliente não pode ser nulo.");
        }
        return cliente.getId() == null ? inserir(conexao, cliente) : atualizar(conexao, cliente);
    }

    private Cliente inserir(Connection conexao, Cliente cliente) {
        long id = comMensagemDuplicado(DUPLICADO, () -> inserir(conexao,
            "INSERT INTO cliente (nome, cpf, email, telefone, logradouro, cidade, cep, ativo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            statement -> preencherCampos(statement, cliente)));
        cliente.setId(id);
        return cliente;
    }

    @Override
    public Cliente atualizar(Connection conexao, Cliente cliente) {
        long id = exigirId(cliente == null ? null : cliente.getId(), ENTIDADE);
        int linhas = comMensagemDuplicado(DUPLICADO, () -> executar(conexao,
            "UPDATE cliente SET nome = ?, cpf = ?, email = ?, telefone = ?, logradouro = ?, "
                + "cidade = ?, cep = ?, ativo = ? WHERE id = ?",
            statement -> {
                preencherCampos(statement, cliente);
                statement.setLong(9, id);
            }));
        exigirLinhaAfetada(linhas, ENTIDADE, id);
        return cliente;
    }

    private void preencherCampos(PreparedStatement statement, Cliente cliente) throws SQLException {
        Endereco endereco = cliente.getEndereco();
        statement.setString(1, cliente.getNome());
        statement.setString(2, cliente.getCpf());
        statement.setString(3, cliente.getEmail());
        statement.setString(4, cliente.getTelefone());
        statement.setString(5, endereco == null ? null : endereco.getLogradouro());
        statement.setString(6, endereco == null ? null : endereco.getCidade());
        statement.setString(7, endereco == null ? null : endereco.getCep());
        statement.setBoolean(8, cliente.isAtivo());
    }

    /**
     * Cliente com pedidos dispara o erro MySQL 1451 (ON DELETE RESTRICT), que
     * é traduzido para uma mensagem de negócio sem citar tabela nem constraint.
     */
    @Override
    public void remover(Connection conexao, Long id) {
        long idValidado = exigirId(id, ENTIDADE);
        int linhas = executar(conexao, "DELETE FROM cliente WHERE id = ?",
            statement -> statement.setLong(1, idValidado), COM_PEDIDOS);
        exigirLinhaAfetada(linhas, ENTIDADE, idValidado);
    }

    @Override
    public void inativar(Connection conexao, Long id) {
        alterarAtivo(conexao, id, false);
    }

    @Override
    public void reativar(Connection conexao, Long id) {
        alterarAtivo(conexao, id, true);
    }

    private void alterarAtivo(Connection conexao, Long id, boolean ativo) {
        long idValidado = exigirId(id, ENTIDADE);
        int linhas = executar(conexao, "UPDATE cliente SET ativo = ? WHERE id = ?",
            statement -> {
                statement.setBoolean(1, ativo);
                statement.setLong(2, idValidado);
            });
        exigirLinhaAfetada(linhas, ENTIDADE, idValidado);
    }

    @Override
    public Optional<Cliente> buscarPorId(Connection conexao, Long id) {
        return buscarPorId(conexao, id, Visibilidade.ATIVOS);
    }

    @Override
    public Optional<Cliente> buscarPorId(Connection conexao, Long id, Visibilidade visibilidade) {
        if (id == null) {
            return Optional.empty();
        }
        return consultarUm(conexao, select(visibilidade, "c.id = ?"),
            statement -> statement.setLong(1, id), resultado -> Mapeadores.cliente(resultado, ""));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(Connection conexao, String cpf) {
        return buscarPorCpf(conexao, cpf, Visibilidade.ATIVOS);
    }

    @Override
    public Optional<Cliente> buscarPorCpf(Connection conexao, String cpf, Visibilidade visibilidade) {
        if (cpf == null || cpf.isBlank()) {
            return Optional.empty();
        }
        return consultarUm(conexao, select(visibilidade, "c.cpf = ?"),
            statement -> statement.setString(1, cpf.trim()), resultado -> Mapeadores.cliente(resultado, ""));
    }

    @Override
    public Optional<Cliente> buscarPorEmail(Connection conexao, String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return consultarUm(conexao, select(Visibilidade.ATIVOS, "c.email = ?"),
            statement -> statement.setString(1, email.trim()), resultado -> Mapeadores.cliente(resultado, ""));
    }

    @Override
    public List<Cliente> listarTodos(Connection conexao, Pagina pagina) {
        return listar(conexao, Visibilidade.ATIVOS, pagina);
    }

    @Override
    public List<Cliente> listar(Connection conexao, Visibilidade visibilidade, Pagina pagina) {
        return consultar(conexao, select(visibilidade, null) + " ORDER BY c.nome, c.id LIMIT ? OFFSET ?",
            statement -> {
                statement.setInt(1, pagina.tamanho());
                statement.setLong(2, pagina.offset());
            }, resultado -> Mapeadores.cliente(resultado, ""));
    }

    @Override
    public long contar(Connection conexao) {
        return contar(conexao,
            "SELECT COUNT(*) FROM cliente c WHERE " + Visibilidade.ATIVOS.condicaoPara("c"), statement -> { });
    }

    /** Ponto único onde o filtro de soft delete é aplicado. */
    private String select(Visibilidade visibilidade, String condicao) {
        String sql = "SELECT " + Mapeadores.colunasCliente("c", "") + " FROM cliente c WHERE "
            + visibilidade.condicaoPara("c");
        return condicao == null ? sql : sql + " AND " + condicao;
    }
}
