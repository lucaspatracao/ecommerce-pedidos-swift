package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.PagamentoDetalhe;
import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PagamentoResumoResponse;
import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import com.ecommerce.pedidos.modelo.FormaPagamento;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.repositorio.FormaPagamentoRepository;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.util.Utils;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de pagamento. Trabalha só com a abstração {@link FormaPagamento}:
 * não há {@code instanceof} nem {@code switch} por tipo (Aula 08).
 * Quem cria o objeto concreto é a {@link FabricaFormaPagamento}.
 */
@Service
@Transactional(readOnly = true)
public class PagamentoServiceImpl implements PagamentoService {

    private final PedidoRepository pedidoRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final FabricaFormaPagamento fabrica;
    private final GatewayPagamento gateway;

    public PagamentoServiceImpl(PedidoRepository pedidoRepository, FormaPagamentoRepository formaPagamentoRepository,
                                FabricaFormaPagamento fabrica, GatewayPagamento gateway) {
        this.pedidoRepository = pedidoRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.fabrica = fabrica;
        this.gateway = gateway;
    }

    @Override
    public List<PagamentoResumoResponse> listar() {
        return formaPagamentoRepository.findAllByOrderByDataDoPagamentoDesc().stream()
            .map(PagamentoResumoResponse::de)
            .toList();
    }

    /**
     * Ordem dos passos:
     * <ol>
     *   <li>a fábrica cria a forma de pagamento (valida os dados);</li>
     *   <li>o domínio aplica as regras internas: valor igual ao total, pedido
     *       aberto, limite do cartão ({@code Pedido.pagarCom});</li>
     *   <li>só então o gateway externo é consultado. Se recusar, a exceção
     *       desfaz tudo (rollback) e o pedido continua ABERTO.</li>
     * </ol>
     * O pagamento é gravado em cascata junto com o pedido (relação 1–1).
     */
    @Override
    @Transactional
    public Map<String, Object> registrar(String numeroPedido, PagamentoRequest request) {
        FormaPagamento pagamento = fabrica.criar(request);
        Pedido pedido = buscarPedido(numeroPedido);

        pedido.pagarCom(pagamento);

        if (!gateway.autorizar(pagamento)) {
            throw new PagamentoRecusadoException(pagamento.getDescricao(), "não autorizado pelo gateway de pagamento");
        }
        return PagamentoDetalhe.de(pagamento);
    }

    @Override
    public Map<String, Object> buscarPorPedido(String numeroPedido) {
        Pedido pedido = buscarPedido(numeroPedido);
        if (pedido.getFormaPagamento() == null) {
            throw new RecursoNaoEncontradoException("Pagamento do pedido", pedido.getNumero());
        }
        return PagamentoDetalhe.de(pedido.getFormaPagamento());
    }

    private Pedido buscarPedido(String numeroPedido) {
        String numero = Utils.textoObrigatorio(numeroPedido, "Número do pedido");
        return pedidoRepository.findByNumero(numero)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", numero));
    }
}
