package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.excecao.ClienteNaoEncontradoException;
import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
                             ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /** JOIN FETCH: uma consulta só para todos os pedidos (sem N+1). */
    @Override
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.buscarTodosCompletos().stream().map(PedidoResponse::de).toList();
    }

    /** Pagina os ids no banco e depois carrega só esses pedidos com JOIN FETCH. */
    @Override
    public List<PedidoResponse> listarRecentes(int limite) {
        List<Long> ids = pedidoRepository.buscarIdsRecentes(PageRequest.of(0, limite));
        if (ids.isEmpty()) {
            return List.of();
        }
        return pedidoRepository.buscarCompletosPorIds(ids).stream().map(PedidoResponse::de).toList();
    }

    @Override
    public PedidoResponse buscarPorNumero(String numero) {
        String chave = Utils.textoObrigatorio(numero, "Número do pedido");
        return pedidoRepository.buscarCompletoPorNumero(chave)
            .map(PedidoResponse::de)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", chave));
    }

    /**
     * Fluxo transacional (Aula 14): tudo aqui dentro é uma unidade só.
     *
     * <p>Cada {@code adicionarItem} baixa o estoque do produto. Se o 2º item
     * lançar {@code EstoqueInsuficienteException}, a exceção (unchecked) sai
     * do método e o Spring faz ROLLBACK: a baixa do 1º item também é desfeita
     * no banco. Sem a transação, o estoque ficaria inconsistente.</p>
     */
    @Override
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter pelo menos um item.");
        }
        String cpf = Utils.apenasDigitos(Utils.textoObrigatorio(request.clienteCpf(), "CPF do cliente"));
        Cliente cliente = clienteRepository.buscarPorCpf(cpf)
            .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));

        Pedido pedido = new Pedido(cliente);
        for (ItemPedidoRequest item : request.itens()) {
            Produto produto = buscarProduto(item.codigo());
            if (!produto.isAtivo()) {
                throw new ConflitoException("Produto indisponível para venda: " + produto.getCodigo());
            }
            // Regra no domínio: baixa o estoque ou lança EstoqueInsuficienteException (409).
            pedido.adicionarItem(produto, item.quantidade() == null ? 0 : item.quantidade());
        }
        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoResponse atualizarStatus(String numero, String status) {
        Pedido pedido = buscarEntidade(numero);
        SituacaoPedido nova = SituacaoPedido.deTexto(status);

        if (nova == SituacaoPedido.CANCELADO) {
            pedido.cancelar();
        } else if (nova == SituacaoPedido.PAGO) {
            throw new ConflitoException("Pagamento deve ser processado com uma forma de pagamento");
        } else if (!pedido.isAberto()) {
            throw new ConflitoException("Transição de situação inválida");
        }
        return PedidoResponse.de(pedido);
    }

    @Override
    @Transactional
    public void excluir(String numero) {
        Pedido pedido = buscarEntidade(numero);
        if (pedido.getSituacao() == SituacaoPedido.PAGO) {
            throw new ConflitoException("Pedido pago não pode ser excluído. Cancele-o antes (o pagamento será estornado).");
        }
        if (pedido.isAberto()) {
            pedido.cancelar(); // devolve o estoque dos itens
        }
        pedidoRepository.delete(pedido); // itens vão junto (cascade + orphanRemoval)
    }

    @Override
    public long contar() {
        return pedidoRepository.count();
    }

    @Override
    public BigDecimal calcularReceitaTotal() {
        // SUM feito no banco (@Query em ItemPedidoRepository); escala 2 como os demais valores.
        return itemPedidoRepository.calcularReceitaTotal().setScale(2, RoundingMode.HALF_UP);
    }

    private Pedido buscarEntidade(String numero) {
        String chave = Utils.textoObrigatorio(numero, "Número do pedido");
        return pedidoRepository.findByNumero(chave)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", chave));
    }

    private Produto buscarProduto(String codigo) {
        String chave = Utils.textoObrigatorio(codigo, "Código do produto");
        return produtoRepository.findByCodigo(chave)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", chave));
    }
}
