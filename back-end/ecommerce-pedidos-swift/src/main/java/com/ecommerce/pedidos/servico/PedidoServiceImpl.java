package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.excecao.ClienteNaoEncontradoException;
import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.ItemPedido;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.modelo.SequenciaPedido;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.SequenciaPedidoRepository;
import com.ecommerce.pedidos.util.PedidoUtils;
import com.ecommerce.pedidos.util.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final SequenciaPedidoRepository sequenciaPedidoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
                             ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository,
                             SequenciaPedidoRepository sequenciaPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.sequenciaPedidoRepository = sequenciaPedidoRepository;
    }

    /**
     * Sem filtro nem página: JOIN FETCH numa consulta só (sem N+1).
     * Com filtro ou página: pagina os ids no banco e carrega só esses pedidos.
     */
    @Override
    public List<PedidoResponse> listar(String situacao, Pageable pagina) {
        SituacaoPedido filtro = situacao == null || situacao.isBlank() ? null : SituacaoPedido.deTexto(situacao);
        if (filtro == null && pagina.isUnpaged()) {
            return pedidoRepository.buscarTodosCompletos().stream().map(PedidoResponse::de).toList();
        }
        return completos(pedidoRepository.buscarIds(filtro, pagina));
    }

    @Override
    public List<PedidoResponse> listarPorCliente(String cpf, Pageable pagina) {
        String chave = Utils.apenasDigitos(Utils.textoObrigatorio(cpf, "CPF do cliente"));
        Cliente cliente = clienteRepository.buscarPorCpf(chave)
            .orElseThrow(() -> new ClienteNaoEncontradoException(chave));
        return completos(pedidoRepository.buscarIdsPorCliente(cliente, pagina));
    }

    @Override
    public List<PedidoResponse> listarRecentes(int limite) {
        return completos(pedidoRepository.buscarIds(null, PageRequest.of(0, limite)));
    }

    /** Passo 2 da listagem paginada: carrega com JOIN FETCH só os ids escolhidos. */
    private List<PedidoResponse> completos(List<Long> ids) {
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
     *
     * <p>Os produtos são lidos COM BLOQUEIO: duas vendas simultâneas do mesmo
     * produto acontecem uma depois da outra, e a mensagem de estoque
     * insuficiente mostra o saldo real daquele momento.</p>
     *
     * <p>O número do pedido vem do contador do banco (ver {@link #proximoNumero()}),
     * na mesma transação: se algo falhar, o número também volta.</p>
     */
    @Override
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter pelo menos um item.");
        }
        String cpf = Utils.apenasDigitos(Utils.textoObrigatorio(request.clienteCpf(), "CPF do cliente"));
        // Cliente inativo não compra: para a venda, é como se não existisse.
        Cliente cliente = clienteRepository.buscarPorCpf(cpf)
            .filter(Cliente::isAtivo)
            .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));

        Pedido pedido = new Pedido(cliente, proximoNumero());
        for (ItemPedidoRequest item : request.itens()) {
            Produto produto = buscarProdutoParaVenda(item.codigo());
            if (!produto.isAtivo()) {
                throw new ConflitoException("Produto indisponível para venda: " + produto.getCodigo());
            }
            // Regra no domínio: baixa o estoque ou lança EstoqueInsuficienteException (409).
            pedido.adicionarItem(produto, item.quantidade() == null ? 0 : item.quantidade());
        }
        return PedidoResponse.de(pedidoRepository.saveAndFlush(pedido));
    }

    /**
     * Próximo número do ano (PED-ano-NNNNN), a partir do contador
     * {@code sequencia_pedido}. A linha do ano fica bloqueada até o fim da
     * transação: pedidos simultâneos esperam a vez e recebem números
     * diferentes, sem sorteio e sem nova tentativa.
     */
    private String proximoNumero() {
        int ano = Year.now().getValue();
        sequenciaPedidoRepository.criarSeNaoExistir(ano);
        SequenciaPedido sequencia = sequenciaPedidoRepository.travar(ano)
            .orElseThrow(() -> new IllegalStateException("Contador de pedidos do ano " + ano + " não encontrado."));
        return PedidoUtils.formatarNumeroDoPedido(ano, sequencia.proximo());
    }

    @Override
    @Transactional
    public PedidoResponse atualizarSituacao(String numero, String situacao) {
        SituacaoPedido nova = SituacaoPedido.deTexto(situacao);
        if (nova == SituacaoPedido.CANCELADO) {
            return cancelar(numero);
        }
        Pedido pedido = buscarEntidade(numero);
        if (nova == SituacaoPedido.PAGO) {
            throw new ConflitoException("Pagamento deve ser processado com uma forma de pagamento");
        }
        if (!pedido.isAberto()) {
            throw new ConflitoException("Transição de situação inválida");
        }
        return PedidoResponse.de(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse cancelar(String numero) {
        Pedido pedido = buscarEntidade(numero);
        pedido.cancelar(); // devolve o estoque e estorna o pagamento; já cancelado → 409
        return PedidoResponse.de(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse removerItem(String numero, Long itemId) {
        Pedido pedido = buscarEntidade(numero);
        if (!pedido.isAberto()) {
            throw new ConflitoException("Somente pedidos em aberto podem ter itens removidos.");
        }
        List<ItemPedido> itens = pedido.getItens();
        int indice = -1;
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getId().equals(itemId)) {
                indice = i;
            }
        }
        if (indice < 0) {
            throw new RecursoNaoEncontradoException("Item do pedido", String.valueOf(itemId));
        }
        pedido.removerItem(indice); // devolve o estoque; orphanRemoval apaga a linha
        return PedidoResponse.de(pedidoRepository.saveAndFlush(pedido));
    }

    @Override
    @Transactional
    public void excluir(String numero) {
        Pedido pedido = buscarEntidade(numero);
        if (pedido.getSituacao() == SituacaoPedido.PAGO) {
            throw new ConflitoException("Pedido já pago não pode ser excluído. "
                + "Cancele o pedido para preservar o histórico de vendas.");
        }
        if (pedido.getSituacao() == SituacaoPedido.CANCELADO) {
            throw new ConflitoException("Pedido cancelado faz parte do histórico e não pode ser excluído.");
        }
        pedido.cancelar(); // pedido aberto: devolve o estoque reservado
        pedidoRepository.delete(pedido); // itens vão junto (cascade + orphanRemoval; ON DELETE CASCADE no banco)
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

    private Produto buscarProdutoParaVenda(String codigo) {
        String chave = Utils.textoObrigatorio(codigo, "Código do produto");
        return produtoRepository.buscarPorCodigoParaVenda(chave)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", chave));
    }
}
