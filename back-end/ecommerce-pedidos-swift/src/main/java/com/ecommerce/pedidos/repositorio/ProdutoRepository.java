package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Produto;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência de {@link Produto}.
 *
 * Convenções comuns a todos os repositórios:
 * <ul>
 *   <li>a conexão é sempre o primeiro parâmetro, para que várias operações
 *       participem da mesma transação;</li>
 *   <li>buscas devolvem {@link Optional} vazio quando não encontram nada;</li>
 *   <li>alterações e remoções sem linha afetada lançam
 *       {@link RepositorioException} do tipo {@code NAO_ENCONTRADO};</li>
 *   <li>consultas de produto filtram {@code ativo = TRUE} por padrão.</li>
 * </ul>
 */
public interface ProdutoRepository {

    /** Insere quando o id é nulo; caso contrário, atualiza (com controle de versão). */
    Produto salvar(Connection conexao, Produto produto);

    /** Atualiza todos os campos pelo id. Produto sem id gera erro de validação. */
    Produto atualizar(Connection conexao, Produto produto);

    /** Atualização parcial: altera apenas o estoque. */
    void atualizarEstoque(Connection conexao, Long id, int novaQuantidade);

    /**
     * Baixa de estoque atômica, protegida contra condição de corrida:
     * só altera se houver quantidade suficiente e o produto estiver ativo.
     *
     * @return {@code false} quando não havia estoque suficiente
     */
    boolean baixarEstoque(Connection conexao, Long id, int quantidade);

    /** Devolve unidades ao estoque (cancelamento/remoção de item). */
    void devolverEstoque(Connection conexao, Long id, int quantidade);

    /** Soft delete: o produto some das consultas padrão, mas continua no banco. */
    void inativar(Connection conexao, Long id);

    void reativar(Connection conexao, Long id);

    /**
     * Exclusão definitiva. Só funciona para produto nunca vendido: se houver
     * itens de pedido referenciando-o, lança {@code VIOLACAO_INTEGRIDADE}.
     */
    void remover(Connection conexao, Long id);

    Optional<Produto> buscarPorId(Connection conexao, Long id);

    Optional<Produto> buscarPorId(Connection conexao, Long id, Visibilidade visibilidade);

    Optional<Produto> buscarPorCodigo(Connection conexao, String codigo);

    Optional<Produto> buscarPorCodigo(Connection conexao, String codigo, Visibilidade visibilidade);

    List<Produto> listarTodos(Connection conexao, Pagina pagina);

    List<Produto> listar(Connection conexao, Visibilidade visibilidade, Pagina pagina);

    List<Produto> buscarPorNome(Connection conexao, String trechoDoNome);

    long contar(Connection conexao);
}
