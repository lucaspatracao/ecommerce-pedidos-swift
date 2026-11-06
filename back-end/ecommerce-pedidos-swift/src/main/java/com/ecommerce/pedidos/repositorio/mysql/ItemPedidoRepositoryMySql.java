package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.sql.Connection;
import java.util.List;

/** Implementação MySQL de {@link ItemPedidoRepository}. */
public class ItemPedidoRepositoryMySql extends RepositorioJdbcBase implements ItemPedidoRepository {
    private static final String ENTIDADE = "Item do pedido";

    @Override
    public ItemPedido inserir(Connection conexao, Long pedidoId, ItemPedido item) {
        long pedidoValidado = exigirId(pedidoId, "Pedido do item");
        if (item == null) {
            throw RepositorioException.validacao("Item do pedido não pode ser nulo.");
        }
        long produtoId = exigirId(item.getProduto().getId(), "Produto do item");
        long id = inserir(conexao,
            "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)",
            statement -> {
                statement.setLong(1, pedidoValidado);
                statement.setLong(2, produtoId);
                statement.setInt(3, item.getQuantidade());
                // Preço praticado na venda, não o preço atual do produto.
                statement.setBigDecimal(4, item.getPrecoPraticado());
            });
        item.setId(id);
        return item;
    }

    /**
     * Busca explícita SEM o filtro de soft delete: um pedido antigo precisa
     * continuar exibindo o produto vendido, mesmo que ele tenha sido inativado.
     * O preço exibido é o gravado em {@code preco_unitario}.
     */
    @Override
    public List<ItemPedido> listarPorPedido(Connection conexao, Long pedidoId) {
        if (pedidoId == null) {
            return List.of();
        }
        return consultar(conexao,
            "SELECT i.id, i.quantidade, i.preco_unitario, " + Mapeadores.colunasProduto("p", "produto_")
                + " FROM item_pedido i JOIN produto p ON p.id = i.produto_id"
                + " WHERE i.pedido_id = ? ORDER BY i.id",
            statement -> statement.setLong(1, pedidoId),
            resultado -> ItemPedido.reconstituir(
                resultado.getLong("id"),
                Mapeadores.produto(resultado, "produto_"),
                resultado.getInt("quantidade"),
                resultado.getBigDecimal("preco_unitario")));
    }

    @Override
    public void remover(Connection conexao, Long itemId) {
        long id = exigirId(itemId, ENTIDADE);
        int linhas = executar(conexao, "DELETE FROM item_pedido WHERE id = ?", statement -> statement.setLong(1, id));
        exigirLinhaAfetada(linhas, ENTIDADE, id);
    }

    @Override
    public long contarPorPedido(Connection conexao, Long pedidoId) {
        long id = exigirId(pedidoId, "Pedido");
        return contar(conexao, "SELECT COUNT(*) FROM item_pedido WHERE pedido_id = ?",
            statement -> statement.setLong(1, id));
    }
}
