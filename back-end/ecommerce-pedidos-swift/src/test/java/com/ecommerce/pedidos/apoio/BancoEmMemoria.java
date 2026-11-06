package com.ecommerce.pedidos.apoio;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.OperacaoComConexao;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implementações em memória das interfaces de repositório, para testes de
 * unidade dos serviços sem MySQL. Demonstra também o ganho do padrão
 * Repository: o serviço não sabe (nem precisa saber) onde os dados estão.
 *
 * Não simula rollback; os cenários transacionais são cobertos pelos testes
 * de integração com banco real.
 */
public class BancoEmMemoria {
    private final AtomicLong sequencia = new AtomicLong();
    private final Map<Long, Produto> produtos = new LinkedHashMap<>();
    private final Map<Long, Cliente> clientes = new LinkedHashMap<>();
    private final Map<Long, Pedido> pedidos = new LinkedHashMap<>();
    private final Map<Long, Long> pedidoDoItem = new LinkedHashMap<>();

    public final GerenciadorTransacao transacao = new GerenciadorTransacao() {
        @Override
        public <T, E extends Exception> T executarEmTransacao(OperacaoComConexao<T, E> operacao) throws E {
            return operacao.executar(null);
        }

        @Override
        public <T, E extends Exception> T executar(OperacaoComConexao<T, E> operacao) throws E {
            return operacao.executar(null);
        }
    };

    public final ProdutoRepository produtoRepository = new ProdutosEmMemoria();
    public final ClienteRepository clienteRepository = new ClientesEmMemoria();
    public final ItemPedidoRepository itemPedidoRepository = new ItensEmMemoria();
    public final PedidoRepository pedidoRepository = new PedidosEmMemoria();

    public ProdutoService produtoService() {
        return new ProdutoService(produtoRepository, transacao);
    }

    public ClienteService clienteService() {
        return new ClienteService(clienteRepository, transacao);
    }

    public PedidoService pedidoService() {
        return new PedidoService(pedidoRepository, itemPedidoRepository, clienteRepository, produtoRepository,
            transacao);
    }

    private static <T> List<T> paginar(Stream<T> itens, Pagina pagina) {
        return itens.skip(pagina.offset()).limit(pagina.tamanho()).toList();
    }

    private static boolean visivel(boolean ativo, Visibilidade visibilidade) {
        return switch (visibilidade) {
            case ATIVOS -> ativo;
            case INATIVOS -> !ativo;
            case TODOS -> true;
        };
    }

    /** Cópia defensiva: o serviço altera o objeto lido sem afetar o "banco". */
    private static Produto copiar(Produto original) {
        Produto copia = new Produto(original.getCodigo(), original.getNome(), original.getPreco(),
            original.getQuantidadeEmEstoque());
        copia.setId(original.getId());
        copia.setDescricao(original.getDescricao());
        copia.setAtivo(original.isAtivo());
        copia.setVersao(original.getVersao());
        return copia;
    }

    private static long exigirId(Long id, String entidade) {
        if (id == null) {
            throw RepositorioException.validacao(entidade + " sem id não pode ser atualizado(a) ou removido(a).");
        }
        return id;
    }

    private class ProdutosEmMemoria implements ProdutoRepository {
        @Override
        public Produto salvar(Connection conexao, Produto produto) {
            if (produto.getId() != null) {
                return atualizar(conexao, produto);
            }
            produto.setId(sequencia.incrementAndGet());
            produtos.put(produto.getId(), copiar(produto));
            return produto;
        }

        @Override
        public Produto atualizar(Connection conexao, Produto produto) {
            long id = exigirId(produto.getId(), "Produto");
            Produto atual = existente(id);
            if (atual.getVersao() != produto.getVersao()) {
                throw RepositorioException.conflitoConcorrencia("Produto", produto.getCodigo());
            }
            produto.setVersao(produto.getVersao() + 1);
            produtos.put(id, copiar(produto));
            return produto;
        }

        @Override
        public void atualizarEstoque(Connection conexao, Long id, int novaQuantidade) {
            existente(exigirId(id, "Produto")).setQuantidadeEmEstoque(novaQuantidade);
        }

        @Override
        public boolean baixarEstoque(Connection conexao, Long id, int quantidade) {
            Produto produto = existente(exigirId(id, "Produto"));
            if (!produto.isAtivo() || produto.getQuantidadeEmEstoque() < quantidade) {
                return false;
            }
            produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - quantidade);
            return true;
        }

        @Override
        public void devolverEstoque(Connection conexao, Long id, int quantidade) {
            existente(exigirId(id, "Produto")).adicionarEstoque(quantidade);
        }

        @Override
        public void inativar(Connection conexao, Long id) {
            existente(exigirId(id, "Produto")).setAtivo(false);
        }

        @Override
        public void reativar(Connection conexao, Long id) {
            existente(exigirId(id, "Produto")).setAtivo(true);
        }

        @Override
        public void remover(Connection conexao, Long id) {
            long idValidado = exigirId(id, "Produto");
            boolean vendido = pedidos.values().stream().flatMap(pedido -> pedido.getItens().stream())
                .anyMatch(item -> item.getProduto().getId().equals(idValidado));
            if (vendido) {
                throw RepositorioException.violacaoIntegridade("Este produto já foi vendido e não pode ser excluído.", null);
            }
            if (produtos.remove(idValidado) == null) {
                throw RepositorioException.naoEncontrado("Produto", idValidado);
            }
        }

        @Override
        public Optional<Produto> buscarPorId(Connection conexao, Long id) {
            return buscarPorId(conexao, id, Visibilidade.ATIVOS);
        }

        @Override
        public Optional<Produto> buscarPorId(Connection conexao, Long id, Visibilidade visibilidade) {
            return buscar(produto -> produto.getId().equals(id), visibilidade);
        }

        @Override
        public Optional<Produto> buscarPorCodigo(Connection conexao, String codigo) {
            return buscarPorCodigo(conexao, codigo, Visibilidade.ATIVOS);
        }

        @Override
        public Optional<Produto> buscarPorCodigo(Connection conexao, String codigo, Visibilidade visibilidade) {
            return buscar(produto -> produto.getCodigo().equals(codigo == null ? null : codigo.trim()), visibilidade);
        }

        @Override
        public List<Produto> listarTodos(Connection conexao, Pagina pagina) {
            return listar(conexao, Visibilidade.ATIVOS, pagina);
        }

        @Override
        public List<Produto> listar(Connection conexao, Visibilidade visibilidade, Pagina pagina) {
            return paginar(produtos.values().stream().filter(produto -> visivel(produto.isAtivo(), visibilidade))
                .sorted(Comparator.comparing(Produto::getNome)).map(BancoEmMemoria::copiar), pagina);
        }

        @Override
        public List<Produto> buscarPorNome(Connection conexao, String trechoDoNome) {
            String trecho = trechoDoNome.toLowerCase();
            return listarTodos(conexao, Pagina.todas()).stream()
                .filter(produto -> produto.getNome().toLowerCase().contains(trecho)).toList();
        }

        @Override
        public long contar(Connection conexao) {
            return produtos.values().stream().filter(Produto::isAtivo).count();
        }

        private Optional<Produto> buscar(Predicate<Produto> criterio, Visibilidade visibilidade) {
            return produtos.values().stream().filter(produto -> visivel(produto.isAtivo(), visibilidade))
                .filter(criterio).findFirst().map(BancoEmMemoria::copiar);
        }

        private Produto existente(long id) {
            Produto produto = produtos.get(id);
            if (produto == null) {
                throw RepositorioException.naoEncontrado("Produto", id);
            }
            return produto;
        }
    }

    private class ClientesEmMemoria implements ClienteRepository {
        @Override
        public Cliente salvar(Connection conexao, Cliente cliente) {
            if (cliente.getId() != null) {
                return atualizar(conexao, cliente);
            }
            cliente.setId(sequencia.incrementAndGet());
            clientes.put(cliente.getId(), cliente);
            return cliente;
        }

        @Override
        public Cliente atualizar(Connection conexao, Cliente cliente) {
            long id = exigirId(cliente.getId(), "Cliente");
            existente(id);
            clientes.put(id, cliente);
            return cliente;
        }

        @Override
        public void remover(Connection conexao, Long id) {
            long idValidado = exigirId(id, "Cliente");
            existente(idValidado);
            boolean temPedidos = pedidos.values().stream()
                .anyMatch(pedido -> pedido.getCliente().getId().equals(idValidado));
            if (temPedidos) {
                throw RepositorioException.violacaoIntegridade("Este cliente possui pedidos e não pode ser excluído.", null);
            }
            clientes.remove(idValidado);
        }

        @Override
        public void inativar(Connection conexao, Long id) {
            existente(exigirId(id, "Cliente")).setAtivo(false);
        }

        @Override
        public void reativar(Connection conexao, Long id) {
            existente(exigirId(id, "Cliente")).setAtivo(true);
        }

        @Override
        public Optional<Cliente> buscarPorId(Connection conexao, Long id) {
            return buscarPorId(conexao, id, Visibilidade.ATIVOS);
        }

        @Override
        public Optional<Cliente> buscarPorId(Connection conexao, Long id, Visibilidade visibilidade) {
            return buscar(cliente -> cliente.getId().equals(id), visibilidade);
        }

        @Override
        public Optional<Cliente> buscarPorCpf(Connection conexao, String cpf) {
            return buscarPorCpf(conexao, cpf, Visibilidade.ATIVOS);
        }

        @Override
        public Optional<Cliente> buscarPorCpf(Connection conexao, String cpf, Visibilidade visibilidade) {
            return buscar(cliente -> cliente.getCpf().equals(cpf == null ? null : cpf.trim()), visibilidade);
        }

        @Override
        public Optional<Cliente> buscarPorEmail(Connection conexao, String email) {
            return buscar(cliente -> cliente.getEmail().equals(email), Visibilidade.ATIVOS);
        }

        @Override
        public List<Cliente> listarTodos(Connection conexao, Pagina pagina) {
            return listar(conexao, Visibilidade.ATIVOS, pagina);
        }

        @Override
        public List<Cliente> listar(Connection conexao, Visibilidade visibilidade, Pagina pagina) {
            return paginar(clientes.values().stream().filter(cliente -> visivel(cliente.isAtivo(), visibilidade)), pagina);
        }

        @Override
        public long contar(Connection conexao) {
            return clientes.values().stream().filter(Cliente::isAtivo).count();
        }

        private Optional<Cliente> buscar(Predicate<Cliente> criterio, Visibilidade visibilidade) {
            return clientes.values().stream().filter(cliente -> visivel(cliente.isAtivo(), visibilidade))
                .filter(criterio).findFirst();
        }

        private Cliente existente(long id) {
            Cliente cliente = clientes.get(id);
            if (cliente == null) {
                throw RepositorioException.naoEncontrado("Cliente", id);
            }
            return cliente;
        }
    }

    private class ItensEmMemoria implements ItemPedidoRepository {
        @Override
        public ItemPedido inserir(Connection conexao, Long pedidoId, ItemPedido item) {
            item.setId(sequencia.incrementAndGet());
            pedidoDoItem.put(item.getId(), exigirId(pedidoId, "Pedido"));
            return item;
        }

        @Override
        public List<ItemPedido> listarPorPedido(Connection conexao, Long pedidoId) {
            Pedido pedido = pedidos.get(pedidoId);
            return pedido == null ? List.of() : pedido.getItens();
        }

        @Override
        public void remover(Connection conexao, Long itemId) {
            Long pedidoId = pedidoDoItem.remove(exigirId(itemId, "Item do pedido"));
            if (pedidoId == null) {
                throw RepositorioException.naoEncontrado("Item do pedido", itemId);
            }
            Pedido pedido = pedidos.get(pedidoId);
            List<ItemPedido> restantes = new ArrayList<>(pedido.getItens());
            restantes.removeIf(item -> item.getId().equals(itemId));
            pedidos.put(pedidoId, Pedido.reconstituir(pedido.getId(), pedido.getNumero(), pedido.getCliente(),
                pedido.getData(), pedido.getSituacao(), restantes));
        }

        @Override
        public long contarPorPedido(Connection conexao, Long pedidoId) {
            return pedidoDoItem.values().stream().filter(id -> id.equals(pedidoId)).count();
        }
    }

    private class PedidosEmMemoria implements PedidoRepository {
        @Override
        public Pedido salvar(Connection conexao, Pedido pedido) {
            if (pedido.getId() == null) {
                pedido.setId(sequencia.incrementAndGet());
            }
            pedidos.put(pedido.getId(), pedido);
            return pedido;
        }

        @Override
        public void atualizarSituacao(Connection conexao, Long id, SituacaoPedido situacaoEsperada,
                SituacaoPedido novaSituacao) {
            Pedido pedido = pedidos.get(exigirId(id, "Pedido"));
            if (pedido == null) {
                throw RepositorioException.naoEncontrado("Pedido", id);
            }
            pedidos.put(id, Pedido.reconstituir(id, pedido.getNumero(), pedido.getCliente(), pedido.getData(),
                novaSituacao, pedido.getItens()));
        }

        @Override
        public void remover(Connection conexao, Long id) {
            if (pedidos.remove(exigirId(id, "Pedido")) == null) {
                throw RepositorioException.naoEncontrado("Pedido", id);
            }
            pedidoDoItem.values().removeIf(pedidoId -> pedidoId.equals(id));
        }

        @Override
        public Optional<Pedido> buscarPorId(Connection conexao, Long id) {
            return Optional.ofNullable(pedidos.get(id)).map(this::copiarPedido);
        }

        @Override
        public Optional<Pedido> buscarPorNumero(Connection conexao, String numero) {
            return pedidos.values().stream().filter(pedido -> pedido.getNumero().equals(numero)).findFirst()
                .map(this::copiarPedido);
        }

        @Override
        public List<Pedido> listarTodos(Connection conexao, Pagina pagina) {
            return paginar(pedidos.values().stream().map(this::copiarPedido), pagina);
        }

        @Override
        public List<Pedido> listarPorCliente(Connection conexao, Long clienteId, Pagina pagina) {
            return paginar(pedidos.values().stream().filter(pedido -> pedido.getCliente().getId().equals(clienteId))
                .map(this::copiarPedido), pagina);
        }

        @Override
        public List<Pedido> listarPorSituacao(Connection conexao, SituacaoPedido situacao, Pagina pagina) {
            return paginar(pedidos.values().stream().filter(pedido -> pedido.getSituacao() == situacao)
                .map(this::copiarPedido), pagina);
        }

        @Override
        public long contar(Connection conexao) {
            return pedidos.size();
        }

        private Pedido copiarPedido(Pedido pedido) {
            List<ItemPedido> itens = pedido.getItens().stream()
                .map(item -> ItemPedido.reconstituir(item.getId(), copiar(item.getProduto()), item.getQuantidade(),
                    item.getPrecoPraticado()))
                .toList();
            return Pedido.reconstituir(pedido.getId(), pedido.getNumero(), pedido.getCliente(), pedido.getData(),
                pedido.getSituacao(), itens);
        }
    }
}
