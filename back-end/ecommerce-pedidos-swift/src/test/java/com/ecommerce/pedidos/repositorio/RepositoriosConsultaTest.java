package com.ecommerce.pedidos.repositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Consultas escritas à mão (@Query) e prova do JOIN FETCH (Aula 14).
 *
 * <p>Para provar que o N+1 foi evitado, o teste liga as estatísticas do
 * Hibernate e CONTA quantos comandos SQL foram enviados ao banco.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class RepositoriosConsultaTest {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private ItemPedidoRepository itemPedidoRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics estatisticas;

    @BeforeEach
    void ligarEstatisticas() {
        estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);
    }

    @Test
    @DisplayName("JOIN FETCH: listar N pedidos completos usa 1 consulta, não 1 + N")
    void deveListarPedidosEmUmaConsulta() {
        criarPedidos(3);

        estatisticas.clear();
        List<PedidoResponse> pedidos = pedidoService.listarTodos();

        assertTrue(pedidos.size() >= 3);
        // Acessar cliente, itens, produto e pagamento de cada pedido não gerou SQL extra.
        assertEquals(1, estatisticas.getPrepareStatementCount(),
            "a listagem deveria ser uma única consulta com JOINs");
    }

    @Test
    @DisplayName("JOIN FETCH: buscar um pedido completo usa 1 consulta; recentes usam 2 (ids + fetch)")
    void deveBuscarPedidoEmUmaConsulta() {
        List<PedidoResponse> criados = criarPedidos(2);

        estatisticas.clear();
        PedidoResponse pedido = pedidoService.buscarPorNumero(criados.get(0).numero());
        assertEquals(1, estatisticas.getPrepareStatementCount());
        assertEquals(2, pedido.itens().size());

        estatisticas.clear();
        List<PedidoResponse> recentes = pedidoService.listarRecentes(2);
        assertEquals(2, recentes.size());
        assertEquals(2, estatisticas.getPrepareStatementCount());
    }

    @Test
    @DisplayName("@Query: busca cliente pelo CPF gravado no atributo herdado 'documento'")
    void deveBuscarClientePorCpf() {
        String cpf = DadosDeTeste.novoCpf();
        clienteService.cadastrar(new ClienteRequest("Dora", cpf, "dora@teste.com", "11900000000", null));

        assertEquals("Dora", clienteRepository.buscarPorCpf(cpf).orElseThrow().getNome());
        assertTrue(clienteRepository.buscarPorCpf("00000000000").isEmpty());
    }

    @Test
    @DisplayName("@Query: estoque crítico traz só produtos abaixo do limite, do menor saldo ao maior")
    void deveListarEstoqueCritico() {
        String critico = DadosDeTeste.novoCodigo("CRIT");
        String folgado = DadosDeTeste.novoCodigo("FOLG");
        produtoService.cadastrar(new ProdutoRequest(critico, "Quase acabando", "", BigDecimal.TEN, 0, true));
        produtoService.cadastrar(new ProdutoRequest(folgado, "Bastante", "", BigDecimal.TEN, 500, true));

        var lista = produtoRepository.buscarComEstoqueAbaixoDe(15);

        assertTrue(lista.stream().anyMatch(p -> p.getCodigo().equals(critico)));
        assertTrue(lista.stream().noneMatch(p -> p.getCodigo().equals(folgado)));
        assertEquals(0, lista.get(0).getQuantidadeEmEstoque());
    }

    @Test
    @DisplayName("@Query: receita total é o SUM de preço praticado × quantidade")
    void deveSomarReceita() {
        BigDecimal antes = itemPedidoRepository.calcularReceitaTotal();
        criarPedidos(1); // 1 pedido: 2 × 10,00 + 1 × 5,00 = 25,00

        BigDecimal depois = itemPedidoRepository.calcularReceitaTotal();

        assertEquals(0, new BigDecimal("25.00").compareTo(depois.subtract(antes)));
    }

    /** Cada pedido tem 2 itens com produtos diferentes, para o JOIN ter trabalho a fazer. */
    private List<PedidoResponse> criarPedidos(int quantidade) {
        String cpf = DadosDeTeste.novoCpf();
        clienteService.cadastrar(new ClienteRequest("Cliente Consulta", cpf, "consulta@teste.com", "11900000000", "Rua"));
        String codigoA = DadosDeTeste.novoCodigo("Q-A");
        String codigoB = DadosDeTeste.novoCodigo("Q-B");
        produtoService.cadastrar(new ProdutoRequest(codigoA, "Item A", "", new BigDecimal("10.00"), 100, true));
        produtoService.cadastrar(new ProdutoRequest(codigoB, "Item B", "", new BigDecimal("5.00"), 100, true));
        return java.util.stream.IntStream.range(0, quantidade)
            .mapToObj(i -> pedidoService.criar(new PedidoRequest(cpf, List.of(
                new ItemPedidoRequest(codigoA, 2), new ItemPedidoRequest(codigoB, 1)))))
            .toList();
    }
}
