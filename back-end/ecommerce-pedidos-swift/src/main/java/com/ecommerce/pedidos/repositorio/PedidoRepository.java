package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência do cabeçalho de {@link Pedido}. Os itens são
 * gravados por {@link ItemPedidoRepository}, mas são carregados junto com o
 * pedido nas buscas.
 *
 * Pedidos sempre exibem cliente e produtos mesmo que estejam inativos: o
 * histórico de vendas não pode depender do soft delete.
 */
public interface PedidoRepository {

    /** Insere o cabeçalho quando o id é nulo; caso contrário, atualiza a situação. */
    Pedido salvar(Connection conexao, Pedido pedido);

    /**
     * Muda a situação somente se o pedido ainda estiver na situação esperada
     * (evita, por exemplo, cancelar duas vezes em requisições simultâneas).
     */
    void atualizarSituacao(Connection conexao, Long id, SituacaoPedido situacaoEsperada, SituacaoPedido novaSituacao);

    /** Remove o pedido; os itens são removidos em cascata (ON DELETE CASCADE). */
    void remover(Connection conexao, Long id);

    Optional<Pedido> buscarPorId(Connection conexao, Long id);

    Optional<Pedido> buscarPorNumero(Connection conexao, String numero);

    List<Pedido> listarTodos(Connection conexao, Pagina pagina);

    List<Pedido> listarPorCliente(Connection conexao, Long clienteId, Pagina pagina);

    List<Pedido> listarPorSituacao(Connection conexao, SituacaoPedido situacao, Pagina pagina);

    long contar(Connection conexao);
}
