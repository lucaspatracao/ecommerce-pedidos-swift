package com.ecommerce.pedidos.config;

import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.ItemPedidoRepository;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.mysql.ClienteRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.ExecutorMigracoes;
import com.ecommerce.pedidos.repositorio.mysql.ItemPedidoRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.JdbcGerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.mysql.PedidoRepositoryMySql;
import com.ecommerce.pedidos.repositorio.mysql.ProdutoRepositoryMySql;
import com.ecommerce.pedidos.util.ConexaoFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Único ponto do código que conhece as implementações MySQL. Serviços e
 * controllers recebem apenas as interfaces por injeção de dependência.
 */
@Configuration
public class PersistenciaConfig {
    private static final Logger LOG = LoggerFactory.getLogger(PersistenciaConfig.class);

    @Bean
    public GerenciadorTransacao gerenciadorTransacao() {
        return new JdbcGerenciadorTransacao(ConexaoFactory::getConnection);
    }

    @Bean
    public ProdutoRepository produtoRepository() {
        return new ProdutoRepositoryMySql();
    }

    @Bean
    public ClienteRepository clienteRepository() {
        return new ClienteRepositoryMySql();
    }

    @Bean
    public ItemPedidoRepository itemPedidoRepository() {
        return new ItemPedidoRepositoryMySql();
    }

    @Bean
    public PedidoRepository pedidoRepository(ItemPedidoRepository itemPedidoRepository) {
        return new PedidoRepositoryMySql(itemPedidoRepository);
    }

    /** Aplica as migrations pendentes ao subir a aplicação (desligável por propriedade). */
    @Bean
    public ApplicationRunner aplicarMigracoes(GerenciadorTransacao transacao,
            @Value("${db.migracoes.automaticas:true}") boolean automaticas) {
        return argumentos -> {
            if (!automaticas) {
                LOG.info("Migrations automáticas desligadas (db.migracoes.automaticas=false).");
                return;
            }
            List<String> aplicadas = transacao.executar(conexao -> new ExecutorMigracoes().migrar(conexao));
            LOG.info(aplicadas.isEmpty() ? "Banco já está atualizado." : "Migrations aplicadas: {}", aplicadas);
        };
    }
}
