package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
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
 * Número do pedido gerado pelo contador do banco (tabela sequencia_pedido):
 * sem sorteio, sem repetição e no formato PED-ano-NNNNN.
 */
@Tag("integracao")
@SpringBootTest
@ActiveProfiles("test")
class NumeroPedidoIntegracaoTest {

    private static final String FORMATO = "PED-" + Year.now().getValue() + "-\\d{5}";

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private ClienteService clienteService;

    private String cpf;

    @BeforeEach
    void preparar() {
        cpf = DadosDeTeste.novoCpf();
        clienteService.cadastrar(new ClienteRequest("Cliente Número", cpf, "numero" + cpf + "@teste.com", "11900000000", null));
    }

    private String novoProduto(int estoque) {
        String codigo = DadosDeTeste.novoCodigo("NUM");
        produtoService.cadastrar(new ProdutoRequest(codigo, "Produto " + codigo, "", BigDecimal.ONE, estoque, true));
        return codigo;
    }

    private String criarPedido(String codigo) {
        return pedidoService.criar(new PedidoRequest(cpf, List.of(new ItemPedidoRequest(codigo, 1)))).numero();
    }

    private static int sequencial(String numero) {
        return Integer.parseInt(numero.substring(numero.lastIndexOf('-') + 1));
    }

    @Test
    @DisplayName("vários pedidos seguidos: números no formato PED-ano-NNNNN, sem repetição e em sequência")
    void pedidosSeguidosNaoRepetemNumero() {
        String codigo = novoProduto(100);
        List<String> numeros = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            numeros.add(criarPedido(codigo));
        }

        assertEquals(30, new HashSet<>(numeros).size(), "nenhum número pode se repetir: " + numeros);
        for (int i = 0; i < numeros.size(); i++) {
            assertTrue(numeros.get(i).matches(FORMATO), numeros.get(i));
            if (i > 0) {
                assertEquals(sequencial(numeros.get(i - 1)) + 1, sequencial(numeros.get(i)),
                    "cada pedido recebe o valor seguinte do contador");
            }
        }
    }

    @Test
    @DisplayName("pedidos simultâneos recebem números diferentes")
    void pedidosSimultaneosNaoRepetemNumero() throws Exception {
        int quantidade = 8;
        List<String> codigos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            codigos.add(novoProduto(5)); // produtos diferentes: só o contador é disputado
        }
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService threads = Executors.newFixedThreadPool(quantidade);
        try {
            List<Future<String>> pedidos = new ArrayList<>();
            for (String codigo : codigos) {
                Callable<String> tarefa = () -> {
                    largada.await();
                    return criarPedido(codigo);
                };
                pedidos.add(threads.submit(tarefa));
            }
            largada.countDown();

            Set<String> numeros = new HashSet<>();
            for (Future<String> pedido : pedidos) {
                String numero = pedido.get(30, TimeUnit.SECONDS);
                assertTrue(numero.matches(FORMATO), numero);
                numeros.add(numero);
            }
            assertEquals(quantidade, numeros.size(), "números repetidos: " + numeros);
        } finally {
            threads.shutdownNow();
        }
    }

    @Test
    @DisplayName("pedido que falha não consome número: o próximo pedido recebe o valor seguinte ao último gravado")
    void falhaNaoConsomeNumero() {
        String codigo = novoProduto(1);
        String primeiro = criarPedido(codigo);
        // Sem estoque: a transação é desfeita, inclusive o incremento do contador.
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> criarPedido(codigo));
        String segundo = criarPedido(novoProduto(1));
        assertEquals(sequencial(primeiro) + 1, sequencial(segundo));
    }
}
