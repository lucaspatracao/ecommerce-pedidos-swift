package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Fluxo transacional com o SERVIÇO REAL (Aula 14).
 *
 * <p>Atenção: a classe de teste NÃO tem {@code @Transactional}. Se tivesse,
 * o teste inteiro rodaria numa transação só e o rollback do serviço não
 * poderia ser observado. Assim, cada chamada ao serviço abre e fecha a sua
 * própria transação, exatamente como numa requisição HTTP.</p>
 */
@Tag("integracao")
@SpringBootTest
@ActiveProfiles("test")
class TransacaoPedidoTest {

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private PedidoRepository pedidoRepository;

    private String cpf;
    private String codigoComEstoque;
    private String codigoQuaseSemEstoque;

    @BeforeEach
    void setUp() {
        cpf = DadosDeTeste.novoCpf();
        clienteService.cadastrar(new ClienteRequest("Cliente Transacao", cpf, "transacao" + cpf + "@teste.com", "11900000000", null));
        codigoComEstoque = DadosDeTeste.novoCodigo("TX-A");
        codigoQuaseSemEstoque = DadosDeTeste.novoCodigo("TX-B");
        produtoService.cadastrar(new ProdutoRequest(codigoComEstoque, "Produto A", "", new BigDecimal("10.00"), 10, true));
        produtoService.cadastrar(new ProdutoRequest(codigoQuaseSemEstoque, "Produto B", "", new BigDecimal("5.00"), 1, true));
    }

    @Test
    @DisplayName("falha no 2º item desfaz a baixa de estoque do 1º item (rollback)")
    void deveDesfazerBaixaDoPrimeiroItemQuandoSegundoFalha() {
        long pedidosAntes = pedidoRepository.count();
        PedidoRequest pedido = new PedidoRequest(cpf, List.of(
            new ItemPedidoRequest(codigoComEstoque, 3),        // baixaria 10 -> 7
            new ItemPedidoRequest(codigoQuaseSemEstoque, 5))); // só há 1: falha aqui

        assertThrows(EstoqueInsuficienteException.class, () -> pedidoService.criar(pedido));

        // Lido numa NOVA transação: se o rollback não tivesse ocorrido, seria 7.
        assertEquals(10, produtoService.buscarPorCodigo(codigoComEstoque).quantidadeEmEstoque());
        assertEquals(1, produtoService.buscarPorCodigo(codigoQuaseSemEstoque).quantidadeEmEstoque());
        assertEquals(pedidosAntes, pedidoRepository.count(), "nenhum pedido deve ter sido gravado");
    }

    @Test
    @DisplayName("produto inexistente no meio do pedido também desfaz tudo")
    void deveDesfazerQuandoProdutoNaoExiste() {
        PedidoRequest pedido = new PedidoRequest(cpf, List.of(
            new ItemPedidoRequest(codigoComEstoque, 4),
            new ItemPedidoRequest("NAO-EXISTE", 1)));

        assertThrows(RecursoNaoEncontradoException.class, () -> pedidoService.criar(pedido));

        assertEquals(10, produtoService.buscarPorCodigo(codigoComEstoque).quantidadeEmEstoque());
    }

    @Test
    @DisplayName("sucesso: pedido, itens e baixa de estoque são gravados juntos")
    void deveGravarTudoNoSucesso() {
        var criado = pedidoService.criar(new PedidoRequest(cpf, List.of(
            new ItemPedidoRequest(codigoComEstoque, 3),
            new ItemPedidoRequest(codigoQuaseSemEstoque, 1))));

        assertEquals(2, pedidoService.buscarPorNumero(criado.numero()).itens().size());
        assertEquals(7, produtoService.buscarPorCodigo(codigoComEstoque).quantidadeEmEstoque());
        assertEquals(0, produtoService.buscarPorCodigo(codigoQuaseSemEstoque).quantidadeEmEstoque());
    }

    @Test
    @DisplayName("duas vendas simultâneas da última unidade: só uma passa e o estoque nunca fica negativo")
    void vendasSimultaneasDaUltimaUnidade() throws Exception {
        PedidoRequest pedido = new PedidoRequest(cpf, List.of(new ItemPedidoRequest(codigoQuaseSemEstoque, 1)));
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService duasThreads = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> vendas = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                vendas.add(duasThreads.submit(() -> {
                    largada.await();
                    try {
                        pedidoService.criar(pedido);
                        return true;
                    } catch (EstoqueInsuficienteException e) {
                        // A leitura com bloqueio mostra o saldo real: a outra venda já levou a unidade.
                        assertEquals(0, e.getQuantidadeDisponivel());
                        return false;
                    }
                }));
            }
            largada.countDown();

            int aprovadas = 0;
            for (Future<Boolean> venda : vendas) {
                aprovadas += venda.get(10, TimeUnit.SECONDS) ? 1 : 0;
            }
            assertEquals(1, aprovadas);
            assertEquals(0, produtoService.buscarPorCodigo(codigoQuaseSemEstoque).quantidadeEmEstoque());
        } finally {
            duasThreads.shutdownNow();
        }
    }
}
