package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.apoio.DadosDeTeste;
import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ItemPedidoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

/**
 * Regras de persistência conferidas direto no banco (H2 com as migrations do Flyway).
 *
 * <p>Portado dos testes de repositório da branch feature/crud-completo, que
 * usavam JDBC contra MySQL: soft delete, integridade referencial (CASCADE e
 * RESTRICT), unicidade, concorrência otimista, auditoria e paginação. O
 * {@code JdbcTemplate} lê e escreve sem passar pelo Hibernate, para provar
 * que a regra está no BANCO e não só no código Java.</p>
 */
@Tag("integracao")
@SpringBootTest
@ActiveProfiles("test")
class PersistenciaIntegracaoTest {

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
    private JdbcTemplate jdbc;

    private String cpf;
    private String codigo;

    @BeforeEach
    void preparar() {
        cpf = DadosDeTeste.novoCpf();
        clienteService.cadastrar(new ClienteRequest("Cliente Persistência", cpf, "persist" + cpf + "@teste.com",
            "11900000000", "Rua P"));
        codigo = DadosDeTeste.novoCodigo("PER");
        produtoService.cadastrar(new ProdutoRequest(codigo, "Produto " + codigo, "", new BigDecimal("20.00"), 10, true));
    }

    private PedidoResponse venderUm() {
        return pedidoService.criar(new PedidoRequest(cpf, List.of(new ItemPedidoRequest(codigo, 1))));
    }

    private long idDoPedido(String numero) {
        return jdbc.queryForObject("select id from pedido where numero = ?", Long.class, numero);
    }

    @Test
    @DisplayName("soft delete: cliente inativo some da listagem, mas continua nos pedidos antigos")
    void clienteInativoContinuaNosPedidos() {
        String numero = venderUm().numero();
        clienteService.inativar(cpf);

        Boolean ativoNoBanco = jdbc.queryForObject("select ativo from cliente where cpf = ?", Boolean.class, cpf);
        assertFalse(ativoNoBanco);
        assertTrue(clienteRepository.listar(true, PageRequest.of(0, 500)).stream().noneMatch(c -> c.getCpf().equals(cpf)));
        assertEquals(cpf, pedidoService.buscarPorNumero(numero).cliente().cpf());
    }

    @Test
    @DisplayName("histórico: pedido antigo mantém produto e preço cobrados mesmo com produto inativo e preço alterado")
    void pedidoAntigoMantemProdutoEPreco() {
        String numero = venderUm().numero();
        produtoService.atualizar(codigo, new ProdutoRequest(codigo, "Produto " + codigo, "", new BigDecimal("99.00"), 9, true));
        produtoService.excluir(codigo, false);

        var item = pedidoService.buscarPorNumero(numero).itens().get(0);
        assertEquals(codigo, item.produto().codigo());
        assertEquals(new BigDecimal("20.00"), item.precoPraticado());
        assertFalse(item.produto().ativo());
    }

    @Test
    @DisplayName("RESTRICT: o banco recusa apagar cliente com pedidos ou produto já vendido")
    void restricaoNoBanco() {
        venderUm();
        assertThrows(DataIntegrityViolationException.class,
            () -> jdbc.update("delete from cliente where cpf = ?", cpf));
        assertThrows(DataIntegrityViolationException.class,
            () -> jdbc.update("delete from produto where codigo = ?", codigo));
    }

    @Test
    @DisplayName("CASCADE: apagar o pedido no banco apaga os itens dele")
    void excluirPedidoRemoveItens() {
        long pedidoId = idDoPedido(venderUm().numero());
        assertEquals(1, contarItens(pedidoId));

        jdbc.update("delete from pedido where id = ?", pedidoId);

        assertEquals(0, contarItens(pedidoId));
    }

    private int contarItens(long pedidoId) {
        return jdbc.queryForObject("select count(*) from item_pedido where pedido_id = ?", Integer.class, pedidoId);
    }

    @Test
    @DisplayName("UNIQUE: e-mail de cliente e produto repetido no mesmo pedido são recusados pelo banco")
    void unicidadeNoBanco() {
        Cliente outro = new Cliente("Outro", DadosDeTeste.novoCpf(), "persist" + cpf + "@teste.com", "11900000000", "Rua");
        assertThrows(DataIntegrityViolationException.class, () -> clienteRepository.saveAndFlush(outro));

        long pedidoId = idDoPedido(venderUm().numero());
        long produtoId = jdbc.queryForObject("select id from produto where codigo = ?", Long.class, codigo);
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
            "insert into item_pedido (pedido_id, produto_id, quantidade, preco_praticado) values (?, ?, 1, 20.00)",
            pedidoId, produtoId));
    }

    @Test
    @DisplayName("CHECK: estoque negativo é recusado pelo banco")
    void estoqueNuncaNegativo() {
        assertThrows(DataIntegrityViolationException.class,
            () -> jdbc.update("update produto set quantidade_em_estoque = -1 where codigo = ?", codigo));
    }

    @Test
    @DisplayName("concorrência otimista: segunda gravação com versão antiga é recusada")
    void controleDeConcorrenciaOtimista() {
        Produto primeiraLeitura = produtoRepository.findByCodigo(codigo).orElseThrow();
        Produto segundaLeitura = produtoRepository.findByCodigo(codigo).orElseThrow();
        assertEquals(primeiraLeitura.getVersao(), segundaLeitura.getVersao());

        primeiraLeitura.definirEstoque(3);
        produtoRepository.saveAndFlush(primeiraLeitura);

        segundaLeitura.definirEstoque(7);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> produtoRepository.saveAndFlush(segundaLeitura));
        assertEquals(3, produtoService.buscarPorCodigo(codigo).quantidadeEmEstoque());
    }

    @Test
    @DisplayName("auditoria: atualizado_em é preenchido pelo banco e muda a cada UPDATE")
    void auditoria() throws InterruptedException {
        Timestamp antes = jdbc.queryForObject("select atualizado_em from produto where codigo = ?", Timestamp.class, codigo);
        assertNotNull(antes);
        Thread.sleep(20);
        produtoService.atualizarEstoque(codigo, 4);

        Timestamp depois = jdbc.queryForObject("select atualizado_em from produto where codigo = ?", Timestamp.class, codigo);
        assertTrue(depois.after(antes), "atualizado_em deveria avançar: " + antes + " -> " + depois);
    }

    @Test
    @DisplayName("paginação e busca por nome acontecem no banco")
    void paginacaoEBuscaPorNome() {
        String grupo = DadosDeTeste.novoCodigo("GRP");
        for (String sufixo : List.of("A", "B", "C")) {
            produtoService.cadastrar(new ProdutoRequest(grupo + sufixo, "Item " + grupo + sufixo, "",
                BigDecimal.ONE, 1, true));
        }

        assertEquals(3, produtoRepository.listar(true, grupo.toLowerCase(), PageRequest.of(0, 10)).size());
        List<Produto> segundaPagina = produtoRepository.listar(true, grupo, PageRequest.of(1, 2));
        assertEquals(1, segundaPagina.size());
        assertEquals(grupo + "C", segundaPagina.get(0).getCodigo());
    }
}
