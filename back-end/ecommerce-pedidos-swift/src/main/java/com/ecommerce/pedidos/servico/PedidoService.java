package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.excecao.ECommerceException;
import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.FormaPagamento;
import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Ciclo de vida do pedido. Toda operação que mexe em mais de uma tabela
 * (pedido, itens, estoque) roda em UMA transação, com a mesma conexão
 * repassada a todos os repositórios.
 */
@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final GerenciadorTransacao transacao;

    public PedidoService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository,
            ClienteRepository clienteRepository, ProdutoRepository produtoRepository,
            GerenciadorTransacao transacao) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.transacao = transacao;
    }

    public List<Pedido> listarTodos() {
        return listar(Pagina.todas());
    }

    public List<Pedido> listar(Pagina pagina) {
        return transacao.executar(conexao -> pedidoRepository.listarTodos(conexao, pagina));
    }

    public List<Pedido> listarPorSituacao(SituacaoPedido situacao, Pagina pagina) {
        return transacao.executar(conexao -> pedidoRepository.listarPorSituacao(conexao, situacao, pagina));
    }

    /** Pedidos do cliente, inclusive de cliente já inativado (histórico). */
    public List<Pedido> listarPorCliente(String cpf, Pagina pagina) {
        return transacao.executar(conexao -> {
            Cliente cliente = clienteRepository.buscarPorCpf(conexao, cpf, Visibilidade.TODOS)
                .orElseThrow(() -> RepositorioException.naoEncontrado("Cliente", cpf));
            return pedidoRepository.listarPorCliente(conexao, cliente.getId(), pagina);
        });
    }

    public List<Pedido> listarRecentes(int limite) {
        return listar(Pagina.de(0, limite));
    }

    /** Busca pelo número; lança NAO_ENCONTRADO se não existir. */
    public Pedido buscarPorNumero(String numero) {
        validarNumero(numero);
        return transacao.executar(conexao -> buscarExistente(conexao, numero));
    }

    /**
     * Finaliza o pedido de forma atômica: grava o pedido, grava cada item e
     * baixa o estoque — tudo ou nada. Qualquer falha no meio (erro de banco,
     * estoque insuficiente) provoca rollback completo: nenhum pedido, item ou
     * baixa de estoque fica gravado pela metade.
     */
    public Pedido criarPedido(String clienteCpf, List<ItemPedidoRequest> itens) throws EstoqueInsuficienteException {
        Objects.requireNonNull(clienteCpf, "CPF do cliente não pode ser nulo.");
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter pelo menos um item.");
        }
        for (ItemPedidoRequest item : itens) {
            if (item == null || item.codigo() == null || item.codigo().isBlank()) {
                throw new IllegalArgumentException("Código do produto é obrigatório em cada item.");
            }
            if (item.quantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
            }
        }

        return transacao.executarEmTransacao(conexao -> {
            Cliente cliente = clienteRepository.buscarPorCpf(conexao, clienteCpf)
                .orElseThrow(() -> RepositorioException.naoEncontrado("Cliente", clienteCpf));

            // Monta o pedido no domínio: valida itens, consolida produtos
            // repetidos e congela o preço praticado de cada item.
            Pedido pedido = new Pedido(cliente);
            for (ItemPedidoRequest itemRequest : itens) {
                Produto produto = produtoRepository.buscarPorCodigo(conexao, itemRequest.codigo())
                    .orElseThrow(() -> RepositorioException.naoEncontrado("Produto", itemRequest.codigo()));
                pedido.adicionarItem(produto, itemRequest.quantidade());
            }

            pedidoRepository.salvar(conexao, pedido);
            for (ItemPedido item : pedido.getItens()) {
                itemPedidoRepository.inserir(conexao, pedido.getId(), item);
                baixarEstoque(conexao, item.getProduto(), item.getQuantidade());
            }
            return pedido;
        });
    }

    /**
     * A baixa é condicional no banco ({@code quantidade >= ?}); se outra venda
     * consumiu o estoque depois da leitura, nenhuma linha é afetada e lançamos
     * a exceção ANTES do commit, o que desfaz tudo o que já foi gravado.
     */
    private void baixarEstoque(Connection conexao, Produto produto, int quantidade)
            throws EstoqueInsuficienteException {
        if (!produtoRepository.baixarEstoque(conexao, produto.getId(), quantidade)) {
            // Leitura com bloqueio: mostra o estoque realmente disponível agora,
            // não o valor do snapshot lido no início da transação.
            Produto atual = produtoRepository.buscarPorIdParaAtualizacao(conexao, produto.getId())
                .orElse(produto);
            throw new EstoqueInsuficienteException(atual, quantidade);
        }
    }

    public void atualizarStatus(String numero, SituacaoPedido situacao) {
        validarNumero(numero);
        if (situacao == null) {
            throw new IllegalArgumentException("Situação do pedido é obrigatória.");
        }
        if (situacao == SituacaoPedido.CANCELADO) {
            cancelar(numero);
            return;
        }
        if (situacao == SituacaoPedido.PAGO) {
            throw new IllegalStateException("Pagamento deve ser processado com uma forma de pagamento");
        }
        Pedido pedido = buscarPorNumero(numero);
        if (pedido.getSituacao() != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("Transição de situação inválida");
        }
    }

    /**
     * Pedido pago ou finalizado não é excluído: é cancelado. A situação muda
     * e o estoque volta, na mesma transação.
     */
    public Pedido cancelar(String numero) {
        validarNumero(numero);
        return transacao.executarEmTransacao(conexao -> {
            Pedido pedido = buscarExistente(conexao, numero);
            SituacaoPedido anterior = pedido.getSituacao();
            pedido.cancelar();
            pedidoRepository.atualizarSituacao(conexao, pedido.getId(), anterior, SituacaoPedido.CANCELADO);
            for (ItemPedido item : pedido.getItens()) {
                produtoRepository.devolverEstoque(conexao, item.getProduto().getId(), item.getQuantidade());
            }
            return pedido;
        });
    }

    /** Processa o pagamento e grava a nova situação do pedido. */
    public Pedido registrarPagamento(String numero, FormaPagamento pagamento) throws ECommerceException {
        validarNumero(numero);
        return transacao.<Pedido, ECommerceException>executarEmTransacao(conexao -> {
            Pedido pedido = buscarExistente(conexao, numero);
            pedido.pagarCom(pagamento);
            pedidoRepository.atualizarSituacao(conexao, pedido.getId(), SituacaoPedido.ABERTO, SituacaoPedido.PAGO);
            return pedido;
        });
    }

    /**
     * Exclusão física só para pedido em aberto (ex.: carrinho abandonado).
     * Os itens saem por ON DELETE CASCADE e o estoque reservado é devolvido.
     * Pedido pago ou cancelado faz parte do histórico e nunca é excluído.
     */
    public void excluir(String numero) {
        validarNumero(numero);
        transacao.executarEmTransacao(conexao -> {
            Pedido pedido = buscarExistente(conexao, numero);
            exigirAberto(pedido);
            for (ItemPedido item : pedido.getItens()) {
                produtoRepository.devolverEstoque(conexao, item.getProduto().getId(), item.getQuantidade());
            }
            pedidoRepository.remover(conexao, pedido.getId());
            return null;
        });
    }

    /** Remove um item de pedido em aberto e devolve a quantidade ao estoque. */
    public Pedido removerItem(String numero, Long itemId) {
        validarNumero(numero);
        return transacao.executarEmTransacao(conexao -> {
            Pedido pedido = buscarExistente(conexao, numero);
            if (pedido.getSituacao() != SituacaoPedido.ABERTO) {
                throw new IllegalStateException("Somente pedidos em aberto podem ter itens removidos.");
            }
            ItemPedido item = pedido.buscarItemPorId(itemId)
                .orElseThrow(() -> RepositorioException.naoEncontrado("Item do pedido", itemId));
            itemPedidoRepository.remover(conexao, item.getId());
            produtoRepository.devolverEstoque(conexao, item.getProduto().getId(), item.getQuantidade());
            return buscarExistente(conexao, numero);
        });
    }

    public BigDecimal calcularReceitaTotal() {
        return listarTodos().stream()
            .map(Pedido::calcularValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long contar() {
        return transacao.executar(pedidoRepository::contar);
    }

    private void exigirAberto(Pedido pedido) {
        if (pedido.getSituacao() == SituacaoPedido.PAGO) {
            throw new IllegalStateException("Pedido já pago não pode ser excluído. "
                + "Cancele o pedido para preservar o histórico de vendas.");
        }
        if (pedido.getSituacao() == SituacaoPedido.CANCELADO) {
            throw new IllegalStateException("Pedido cancelado faz parte do histórico e não pode ser excluído.");
        }
    }

    private Pedido buscarExistente(Connection conexao, String numero) {
        return pedidoRepository.buscarPorNumero(conexao, numero)
            .orElseThrow(() -> RepositorioException.naoEncontrado("Pedido", numero));
    }

    private void validarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Número do pedido não pode ser vazio.");
        }
    }

    public record ItemPedidoRequest(String codigo, int quantidade) {
    }
}
