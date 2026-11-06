package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação MySQL de {@link PedidoRepository}.
 *
 * O cliente é carregado SEM o filtro de soft delete: um cliente inativado
 * continua aparecendo nos pedidos que já fez.
 */
public class PedidoRepositoryMySql extends RepositorioJdbcBase implements PedidoRepository {
    private static final String ENTIDADE = "Pedido";

    private static final String SELECT_BASE =
        "SELECT pe.id, pe.numero, pe.data_pedido, pe.situacao, " + Mapeadores.colunasCliente("c", "cliente_")
            + " FROM pedido pe JOIN cliente c ON c.id = pe.cliente_id";

    private static final String ORDENACAO = " ORDER BY pe.data_pedido DESC, pe.id DESC";

    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoRepositoryMySql(ItemPedidoRepository itemPedidoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /** Dados do cabeçalho lidos antes de carregar os itens. */
    private record Cabecalho(long id, String numero, LocalDate data, SituacaoPedido situacao, Cliente cliente) {
    }

    @Override
    public Pedido salvar(Connection conexao, Pedido pedido) {
        if (pedido == null) {
            throw RepositorioException.validacao("Pedido não pode ser nulo.");
        }
        if (pedido.getId() != null) {
            long id = pedido.getId();
            int linhas = executar(conexao, "UPDATE pedido SET situacao = ? WHERE id = ?", statement -> {
                statement.setString(1, pedido.getSituacao().name());
                statement.setLong(2, id);
            });
            exigirLinhaAfetada(linhas, ENTIDADE, id);
            return pedido;
        }
        long clienteId = exigirId(pedido.getCliente().getId(), "Cliente do pedido");
        long id = comMensagemDuplicado("Já existe um pedido com este número. Tente novamente.", () -> inserir(conexao,
            "INSERT INTO pedido (numero, cliente_id, data_pedido, situacao) VALUES (?, ?, ?, ?)",
            statement -> {
                statement.setString(1, pedido.getNumero());
                statement.setLong(2, clienteId);
                statement.setDate(3, Date.valueOf(pedido.getData()));
                statement.setString(4, pedido.getSituacao().name());
            }));
        pedido.setId(id);
        return pedido;
    }

    @Override
    public void atualizarSituacao(Connection conexao, Long id, SituacaoPedido situacaoEsperada,
            SituacaoPedido novaSituacao) {
        long idValidado = exigirId(id, ENTIDADE);
        if (situacaoEsperada == null || novaSituacao == null) {
            throw RepositorioException.validacao("Situação atual e nova situação são obrigatórias.");
        }
        int linhas = executar(conexao, "UPDATE pedido SET situacao = ? WHERE id = ? AND situacao = ?",
            statement -> {
                statement.setString(1, novaSituacao.name());
                statement.setLong(2, idValidado);
                statement.setString(3, situacaoEsperada.name());
            });
        if (linhas == 0) {
            boolean existe = contar(conexao, "SELECT COUNT(*) FROM pedido WHERE id = ?",
                statement -> statement.setLong(1, idValidado)) > 0;
            if (existe) {
                throw RepositorioException.conflitoConcorrencia(ENTIDADE, idValidado);
            }
            throw RepositorioException.naoEncontrado(ENTIDADE, idValidado);
        }
    }

    /** Os itens saem junto por ON DELETE CASCADE (composição). */
    @Override
    public void remover(Connection conexao, Long id) {
        long idValidado = exigirId(id, ENTIDADE);
        int linhas = executar(conexao, "DELETE FROM pedido WHERE id = ?", statement -> statement.setLong(1, idValidado));
        exigirLinhaAfetada(linhas, ENTIDADE, idValidado);
    }

    @Override
    public Optional<Pedido> buscarPorId(Connection conexao, Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return consultarUm(conexao, SELECT_BASE + " WHERE pe.id = ?", statement -> statement.setLong(1, id),
            this::mapearCabecalho).map(cabecalho -> montar(conexao, cabecalho));
    }

    @Override
    public Optional<Pedido> buscarPorNumero(Connection conexao, String numero) {
        if (numero == null || numero.isBlank()) {
            return Optional.empty();
        }
        return consultarUm(conexao, SELECT_BASE + " WHERE pe.numero = ?",
            statement -> statement.setString(1, numero.trim()),
            this::mapearCabecalho).map(cabecalho -> montar(conexao, cabecalho));
    }

    @Override
    public List<Pedido> listarTodos(Connection conexao, Pagina pagina) {
        return montarTodos(conexao, consultar(conexao, SELECT_BASE + ORDENACAO + " LIMIT ? OFFSET ?",
            statement -> {
                statement.setInt(1, pagina.tamanho());
                statement.setLong(2, pagina.offset());
            }, this::mapearCabecalho));
    }

    @Override
    public List<Pedido> listarPorCliente(Connection conexao, Long clienteId, Pagina pagina) {
        long id = exigirId(clienteId, "Cliente");
        return montarTodos(conexao, consultar(conexao,
            SELECT_BASE + " WHERE pe.cliente_id = ?" + ORDENACAO + " LIMIT ? OFFSET ?",
            statement -> {
                statement.setLong(1, id);
                statement.setInt(2, pagina.tamanho());
                statement.setLong(3, pagina.offset());
            }, this::mapearCabecalho));
    }

    @Override
    public List<Pedido> listarPorSituacao(Connection conexao, SituacaoPedido situacao, Pagina pagina) {
        if (situacao == null) {
            throw RepositorioException.validacao("Situação do pedido é obrigatória.");
        }
        return montarTodos(conexao, consultar(conexao,
            SELECT_BASE + " WHERE pe.situacao = ?" + ORDENACAO + " LIMIT ? OFFSET ?",
            statement -> {
                statement.setString(1, situacao.name());
                statement.setInt(2, pagina.tamanho());
                statement.setLong(3, pagina.offset());
            }, this::mapearCabecalho));
    }

    @Override
    public long contar(Connection conexao) {
        return contar(conexao, "SELECT COUNT(*) FROM pedido", statement -> { });
    }

    private Cabecalho mapearCabecalho(java.sql.ResultSet resultado) throws java.sql.SQLException {
        return new Cabecalho(
            resultado.getLong("id"),
            resultado.getString("numero"),
            resultado.getDate("data_pedido").toLocalDate(),
            SituacaoPedido.valueOf(resultado.getString("situacao")),
            Mapeadores.cliente(resultado, "cliente_"));
    }

    private List<Pedido> montarTodos(Connection conexao, List<Cabecalho> cabecalhos) {
        return cabecalhos.stream().map(cabecalho -> montar(conexao, cabecalho)).toList();
    }

    private Pedido montar(Connection conexao, Cabecalho cabecalho) {
        return Pedido.reconstituir(cabecalho.id(), cabecalho.numero(), cabecalho.cliente(), cabecalho.data(),
            cabecalho.situacao(), itemPedidoRepository.listarPorPedido(conexao, cabecalho.id()));
    }
}
