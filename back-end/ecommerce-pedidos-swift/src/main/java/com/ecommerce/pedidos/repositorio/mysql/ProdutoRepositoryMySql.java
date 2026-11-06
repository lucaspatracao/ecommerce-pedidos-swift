package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Implementação MySQL de {@link ProdutoRepository} (substitui o antigo
 * ProdutoDAO).
 *
 * Todas as consultas passam por {@link #select(Visibilidade, String)}, que
 * aplica o filtro de soft delete em um único lugar: não há como escrever uma
 * consulta de produto "esquecendo" o {@code ativo = TRUE}.
 *
 * Nota sobre linhas afetadas: o Connector/J devolve, por padrão, as linhas
 * ENCONTRADAS pelo WHERE (não só as que mudaram de valor). Por isso inativar
 * um produto já inativo não é confundido com "produto inexistente".
 */
public class ProdutoRepositoryMySql extends RepositorioJdbcBase implements ProdutoRepository {
    private static final String ENTIDADE = "Produto";
    private static final String DUPLICADO = "Já existe um produto com este código.";

    @Override
    public Produto salvar(Connection conexao, Produto produto) {
        if (produto == null) {
            throw RepositorioException.validacao("Produto não pode ser nulo.");
        }
        return produto.getId() == null ? inserir(conexao, produto) : atualizar(conexao, produto);
    }

    private Produto inserir(Connection conexao, Produto produto) {
        long id = comMensagemDuplicado(DUPLICADO, () -> inserir(conexao,
            "INSERT INTO produto (codigo, nome, descricao, preco, quantidade_em_estoque, ativo) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
            statement -> {
                statement.setString(1, produto.getCodigo());
                statement.setString(2, produto.getNome());
                statement.setString(3, produto.getDescricao());
                statement.setBigDecimal(4, produto.getPreco());
                statement.setInt(5, produto.getQuantidadeEmEstoque());
                statement.setBoolean(6, produto.isAtivo());
            }));
        produto.setId(id);
        produto.setVersao(0);
        return produto;
    }

    /**
     * UPDATE com controle de concorrência otimista: só grava se a versão no
     * banco ainda for a versão que foi lida. Se zero linhas forem afetadas,
     * verificamos se o produto existe para diferenciar "não encontrado" de
     * "alterado por outra operação".
     */
    @Override
    public Produto atualizar(Connection conexao, Produto produto) {
        long id = exigirId(produto == null ? null : produto.getId(), ENTIDADE);
        int linhas = comMensagemDuplicado(DUPLICADO, () -> executar(conexao,
            "UPDATE produto SET codigo = ?, nome = ?, descricao = ?, preco = ?, "
                + "quantidade_em_estoque = ?, ativo = ?, versao = versao + 1 "
                + "WHERE id = ? AND versao = ?",
            statement -> {
                statement.setString(1, produto.getCodigo());
                statement.setString(2, produto.getNome());
                statement.setString(3, produto.getDescricao());
                statement.setBigDecimal(4, produto.getPreco());
                statement.setInt(5, produto.getQuantidadeEmEstoque());
                statement.setBoolean(6, produto.isAtivo());
                statement.setLong(7, id);
                statement.setInt(8, produto.getVersao());
            }));
        if (linhas == 0) {
            if (existe(conexao, id)) {
                throw RepositorioException.conflitoConcorrencia(ENTIDADE, produto.getCodigo());
            }
            throw RepositorioException.naoEncontrado(ENTIDADE, id);
        }
        produto.setVersao(produto.getVersao() + 1);
        return produto;
    }

    @Override
    public void atualizarEstoque(Connection conexao, Long id, int novaQuantidade) {
        long idValidado = exigirId(id, ENTIDADE);
        if (novaQuantidade < 0) {
            throw RepositorioException.validacao("Estoque não pode ser negativo.");
        }
        int linhas = executar(conexao,
            "UPDATE produto SET quantidade_em_estoque = ?, versao = versao + 1 WHERE id = ? AND ativo = TRUE",
            statement -> {
                statement.setInt(1, novaQuantidade);
                statement.setLong(2, idValidado);
            });
        exigirLinhaAfetada(linhas, ENTIDADE, idValidado);
    }

    /**
     * A condição {@code quantidade_em_estoque >= ?} no próprio UPDATE torna a
     * baixa atômica: duas vendas simultâneas não conseguem deixar o estoque
     * negativo, porque o MySQL bloqueia a linha e reavalia o WHERE com o
     * valor mais recente. Zero linhas = estoque insuficiente (ou produto
     * inativo); quem decide o que fazer é o serviço.
     */
    @Override
    public boolean baixarEstoque(Connection conexao, Long id, int quantidade) {
        long idValidado = exigirId(id, ENTIDADE);
        if (quantidade <= 0) {
            throw RepositorioException.validacao("Quantidade para baixa de estoque deve ser positiva.");
        }
        int linhas = executar(conexao,
            "UPDATE produto SET quantidade_em_estoque = quantidade_em_estoque - ?, versao = versao + 1 "
                + "WHERE id = ? AND ativo = TRUE AND quantidade_em_estoque >= ?",
            statement -> {
                statement.setInt(1, quantidade);
                statement.setLong(2, idValidado);
                statement.setInt(3, quantidade);
            });
        return linhas == 1;
    }

    @Override
    public void devolverEstoque(Connection conexao, Long id, int quantidade) {
        long idValidado = exigirId(id, ENTIDADE);
        if (quantidade <= 0) {
            throw RepositorioException.validacao("Quantidade para devolução ao estoque deve ser positiva.");
        }
        // Sem filtro de ativo: a devolução vale mesmo para produto já inativado.
        int linhas = executar(conexao,
            "UPDATE produto SET quantidade_em_estoque = quantidade_em_estoque + ?, versao = versao + 1 WHERE id = ?",
            statement -> {
                statement.setInt(1, quantidade);
                statement.setLong(2, idValidado);
            });
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
        int linhas = executar(conexao,
            "UPDATE produto SET ativo = ?, versao = versao + 1 WHERE id = ?",
            statement -> {
                statement.setBoolean(1, ativo);
                statement.setLong(2, idValidado);
            });
        exigirLinhaAfetada(linhas, ENTIDADE, idValidado);
    }

    @Override
    public void remover(Connection conexao, Long id) {
        long idValidado = exigirId(id, ENTIDADE);
        int linhas = executar(conexao,
            "DELETE FROM produto WHERE id = ?",
            statement -> statement.setLong(1, idValidado),
            "Este produto já foi vendido e não pode ser excluído. Inative-o para retirá-lo do catálogo.");
        exigirLinhaAfetada(linhas, ENTIDADE, idValidado);
    }

    @Override
    public Optional<Produto> buscarPorId(Connection conexao, Long id) {
        return buscarPorId(conexao, id, Visibilidade.ATIVOS);
    }

    @Override
    public Optional<Produto> buscarPorId(Connection conexao, Long id, Visibilidade visibilidade) {
        if (id == null) {
            return Optional.empty();
        }
        return consultarUm(conexao, select(visibilidade, "p.id = ?"),
            statement -> statement.setLong(1, id), this::mapear);
    }

    @Override
    public Optional<Produto> buscarPorCodigo(Connection conexao, String codigo) {
        return buscarPorCodigo(conexao, codigo, Visibilidade.ATIVOS);
    }

    @Override
    public Optional<Produto> buscarPorCodigo(Connection conexao, String codigo, Visibilidade visibilidade) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return consultarUm(conexao, select(visibilidade, "p.codigo = ?"),
            statement -> statement.setString(1, codigo.trim()), this::mapear);
    }

    @Override
    public List<Produto> listarTodos(Connection conexao, Pagina pagina) {
        return listar(conexao, Visibilidade.ATIVOS, pagina);
    }

    @Override
    public List<Produto> listar(Connection conexao, Visibilidade visibilidade, Pagina pagina) {
        return consultar(conexao, select(visibilidade, null) + " ORDER BY p.nome, p.id LIMIT ? OFFSET ?",
            statement -> {
                statement.setInt(1, pagina.tamanho());
                statement.setLong(2, pagina.offset());
            }, this::mapear);
    }

    @Override
    public List<Produto> buscarPorNome(Connection conexao, String trechoDoNome) {
        if (trechoDoNome == null || trechoDoNome.isBlank()) {
            return List.of();
        }
        return consultar(conexao, select(Visibilidade.ATIVOS, "p.nome LIKE ?") + " ORDER BY p.nome, p.id",
            statement -> statement.setString(1, Mapeadores.contendo(trechoDoNome)), this::mapear);
    }

    @Override
    public long contar(Connection conexao) {
        return contar(conexao,
            "SELECT COUNT(*) FROM produto p WHERE " + Visibilidade.ATIVOS.condicaoPara("p"), statement -> { });
    }

    /** Ponto único onde o filtro de soft delete é aplicado. */
    private String select(Visibilidade visibilidade, String condicao) {
        String sql = "SELECT " + Mapeadores.colunasProduto("p", "") + " FROM produto p WHERE "
            + visibilidade.condicaoPara("p");
        return condicao == null ? sql : sql + " AND " + condicao;
    }

    private boolean existe(Connection conexao, long id) {
        return contar(conexao, "SELECT COUNT(*) FROM produto WHERE id = ?", statement -> statement.setLong(1, id)) > 0;
    }

    private Produto mapear(java.sql.ResultSet resultado) throws java.sql.SQLException {
        return Mapeadores.produto(resultado, "");
    }
}
