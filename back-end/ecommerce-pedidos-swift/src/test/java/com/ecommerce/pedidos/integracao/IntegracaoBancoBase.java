package com.ecommerce.pedidos.integracao;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Endereco;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.mysql.ClienteRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.ExecutorMigracoes;
import com.ecommerce.pedidos.repositorio.mysql.ItemPedidoRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.JdbcGerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.mysql.PedidoRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.ProdutoRepositoryMySql;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.util.ConexaoFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

/**
 * Base dos testes de integração com MySQL real.
 *
 * Usa um banco separado ({@code ecommerce_test}, configurável pela variável
 * DB_TEST_URL), recriado pelas próprias migrations no início da execução e
 * limpo após cada teste. Se o MySQL não estiver acessível, os testes são
 * marcados como ignorados em vez de falharem.
 */
@Tag("integracao")
public abstract class IntegracaoBancoBase {
    private static final String URL_PADRAO = "jdbc:mysql://localhost:3306/ecommerce_test"
        + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static String urlTeste;
    private static boolean esquemaCriado;

    protected static GerenciadorTransacao transacao;

    protected final ProdutoRepositoryMySql produtoRepository = new ProdutoRepositoryMySql();
    protected final ClienteRepositoryMySql clienteRepository = new ClienteRepositoryMySql();
    protected final ItemPedidoRepositoryMySql itemPedidoRepository = new ItemPedidoRepositoryMySql();
    protected final PedidoRepositoryMySql pedidoRepository = new PedidoRepositoryMySql(itemPedidoRepository);

    @BeforeAll
    static void prepararBanco() {
        urlTeste = ConexaoFactory.obterConfiguracao("DB_TEST_URL", "db.test.url", URL_PADRAO);
        try (Connection conexao = abrirConexao()) {
            if (!esquemaCriado) {
                recriarEsquema(conexao);
                esquemaCriado = true;
            }
        } catch (SQLException e) {
            Assumptions.abort("MySQL indisponível para testes de integração: " + e.getMessage());
        }
        transacao = new JdbcGerenciadorTransacao(IntegracaoBancoBase::abrirConexao);
    }

    /** Apaga e recria o esquema aplicando V1..Vn: as migrations também são testadas. */
    private static void recriarEsquema(Connection conexao) throws SQLException {
        try (Statement statement = conexao.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS item_pedido, pedido, produto, cliente, schema_migracao");
        }
        new ExecutorMigracoes().migrar(conexao);
    }

    /** Limpeza do banco de teste (isolado) entre os testes. */
    @AfterEach
    void limparDados() throws SQLException {
        try (Connection conexao = abrirConexao(); Statement statement = conexao.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("TRUNCATE TABLE item_pedido");
            statement.execute("TRUNCATE TABLE pedido");
            statement.execute("TRUNCATE TABLE produto");
            statement.execute("TRUNCATE TABLE cliente");
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    protected static Connection abrirConexao() throws SQLException {
        return ConexaoFactory.getConnection(urlTeste);
    }

    protected PedidoService pedidoService() {
        return pedidoService(produtoRepository, itemPedidoRepository);
    }

    /** Permite trocar repositórios por versões que falham de propósito. */
    protected PedidoService pedidoService(ProdutoRepository produtos, ItemPedidoRepository itens) {
        return new PedidoService(new PedidoRepositoryMySql(itens), itens, clienteRepository, produtos, transacao);
    }

    protected Cliente criarCliente(String cpf, String email) {
        Cliente cliente = new Cliente("Cliente " + cpf, cpf, email, "11999990000",
            new Endereco("Rua das Flores, 10", "São Paulo", "01000-000"));
        return transacao.executar(conexao -> clienteRepository.salvar(conexao, cliente));
    }

    protected Produto criarProduto(String codigo, String preco, int estoque) {
        Produto produto = new Produto(codigo, "Produto " + codigo, new BigDecimal(preco), estoque);
        return transacao.executar(conexao -> produtoRepository.salvar(conexao, produto));
    }

    protected long contar(String tabela) throws SQLException {
        try (Connection conexao = abrirConexao(); Statement statement = conexao.createStatement();
             ResultSet resultado = statement.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            resultado.next();
            return resultado.getLong(1);
        }
    }

    /** Lê o estoque direto do banco, ignorando o filtro de ativos. */
    protected int estoqueNoBanco(Long produtoId) throws SQLException {
        try (Connection conexao = abrirConexao();
             PreparedStatement statement = conexao.prepareStatement(
                 "SELECT quantidade_em_estoque FROM produto WHERE id = ?")) {
            statement.setLong(1, produtoId);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }
}
