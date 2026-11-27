package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PagamentoRegistradoResponse;
import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.modelo.FormaPagamento;
import com.ecommerce.pedidos.modelo.SituacaoPagamento;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.GatewayPagamento;
import com.ecommerce.pedidos.servico.PagamentoService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Fluxo cadastro → pedido → pagamento com classes REAIS (Aula 11).
 *
 * <p>Serviços, domínio, repositórios e banco (H2) são os de verdade. A única
 * peça simulada é o {@link GatewayPagamento}, porque representa um sistema
 * EXTERNO (operadora/banco). {@code @MockBean} troca o bean do Spring por um
 * mock do Mockito, que permite dizer "aprove" ou "recuse" e depois conferir
 * se ele foi chamado.</p>
 */
@Tag("integracao")
@SpringBootTest
@ActiveProfiles("test")
class FluxoCompraIntegracaoTest {

    @MockBean
    private GatewayPagamento gateway;

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private PagamentoService pagamentoService;

    private String codigoProduto;
    private PedidoResponse pedido;

    @BeforeEach
    void cadastrarEMontarPedido() {
        String cpf = DadosDeTeste.novoCpf();
        clienteService.cadastrar(new ClienteRequest("Carla Mendes", cpf, "carla" + cpf + "@teste.com", "11988887777", "Rua das Flores"));
        codigoProduto = DadosDeTeste.novoCodigo("FLX");
        produtoService.cadastrar(new ProdutoRequest(codigoProduto, "Fone Bluetooth", "", new BigDecimal("150.00"), 10, true));
        pedido = pedidoService.criar(new PedidoRequest(cpf, List.of(new ItemPedidoRequest(codigoProduto, 2))));
    }

    @Test
    @DisplayName("gateway aprova: pedido fica PAGO e o pagamento aparece na listagem")
    void devePagarQuandoGatewayAprova() {
        when(gateway.autorizar(any(FormaPagamento.class))).thenReturn(true);

        PagamentoRegistradoResponse resposta = pagamentoService.registrar(pix("300.00"));

        assertEquals("Pagamento registrado com sucesso.", resposta.mensagem());
        assertEquals(SituacaoPagamento.APROVADO, resposta.pagamento().get("situacao"));
        PedidoResponse pago = pedidoService.buscarPorNumero(pedido.numero());
        assertEquals(SituacaoPedido.PAGO, pago.situacao());
        assertEquals("Pix", pago.formaPagamento().get("descricao"));
        assertEquals(8, produtoService.buscarPorCodigo(codigoProduto).quantidadeEmEstoque());
        // Mockito confere a interação com a dependência externa.
        verify(gateway).autorizar(argThat(p -> p.getValor().compareTo(new BigDecimal("300.00")) == 0));
    }

    @Test
    @DisplayName("gateway recusa: 409 e o pedido continua ABERTO, sem pagamento gravado")
    void deveManterPedidoAbertoQuandoGatewayRecusa() {
        when(gateway.autorizar(any(FormaPagamento.class))).thenReturn(false);

        PagamentoRecusadoException erro = assertThrows(PagamentoRecusadoException.class,
            () -> pagamentoService.registrar(pix("300.00")));

        assertEquals("Pix", erro.getFormaPagamento());
        PedidoResponse aindaAberto = pedidoService.buscarPorNumero(pedido.numero());
        assertEquals(SituacaoPedido.ABERTO, aindaAberto.situacao());
        assertNull(aindaAberto.formaPagamento(), "o rollback descarta o pagamento");
    }

    @Test
    @DisplayName("valor diferente do total é barrado pelo domínio ANTES de chamar o gateway")
    void naoDeveChamarGatewayComValorErrado() {
        // IllegalStateException do domínio; o ApiExceptionHandler a converte em 409.
        assertThrows(IllegalStateException.class, () -> pagamentoService.registrar(pix("299.99")));
        verify(gateway, never()).autorizar(any());
    }

    @Test
    @DisplayName("cancelar pedido pago estorna o pagamento e devolve o estoque")
    void deveEstornarAoCancelar() {
        when(gateway.autorizar(any(FormaPagamento.class))).thenReturn(true);
        pagamentoService.registrar(pix("300.00"));

        PedidoResponse cancelado = pedidoService.atualizarStatus(pedido.numero(), "CANCELADO");

        assertEquals(SituacaoPedido.CANCELADO, cancelado.situacao());
        assertEquals(SituacaoPagamento.ESTORNADO, cancelado.formaPagamento().get("situacao"));
        assertEquals(10, produtoService.buscarPorCodigo(codigoProduto).quantidadeEmEstoque());
    }

    private PagamentoRequest pix(String valor) {
        return new PagamentoRequest("PIX", new BigDecimal(valor), null, null, null, null, null,
            "carla@teste.com", "EMAIL", pedido.numero());
    }
}
