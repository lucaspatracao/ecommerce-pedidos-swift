package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A migration V3 em um banco que já tinha pedidos com número sorteado.
 *
 * <p>Usa um H2 próprio (não o do contexto Spring): aplica as migrations até a
 * V2, grava pedidos "antigos" e então aplica a V3, como aconteceria no MySQL
 * de quem já usava o sistema.</p>
 */
@Tag("integracao")
class MigracaoNumeroPedidoTest {

    private static final String URL =
        "jdbc:h2:mem:migracao_v3;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private Flyway flyway(String alvo) {
        return Flyway.configure()
            .dataSource(URL, "sa", "")
            .locations("classpath:db/migration")
            .target(alvo)
            .load();
    }

    @Test
    @DisplayName("V3 renumera os pedidos antigos no mesmo formato, por ano e na ordem de criação, e inicia o contador")
    void renumeraPedidosAntigos() throws SQLException {
        flyway("2").migrate();
        try (Connection conexao = DriverManager.getConnection(URL, "sa", "");
             Statement sql = conexao.createStatement()) {
            sql.execute("insert into cliente (cpf, nome, email, telefone) values ('52998224725', 'Ana', 'ana@x.com', '11900000000')");
            // Números sorteados, fora de ordem, em dois anos.
            sql.execute("insert into pedido (numero, cliente_id, data_pedido, situacao) values ('PED-2025-81234', 1, '2025-12-30', 'PAGO')");
            sql.execute("insert into pedido (numero, cliente_id, data_pedido, situacao) values ('PED-2026-00007', 1, '2026-01-02', 'ABERTO')");
            sql.execute("insert into pedido (numero, cliente_id, data_pedido, situacao) values ('PED-2026-55555', 1, '2026-02-10', 'CANCELADO')");

            flyway("latest").migrate();

            assertEquals(List.of("PED-2025-00001", "PED-2026-00001", "PED-2026-00002"),
                coluna(sql, "select numero from pedido order by id"));
            assertEquals(List.of("2025=1", "2026=2"),
                coluna(sql, "select concat(ano, '=', ultimo) from sequencia_pedido order by ano"));
            assertEquals(List.of("0"),
                coluna(sql, "select count(*) from information_schema.tables where table_name = 'renumeracao_pedido'"));
        }
    }

    private static List<String> coluna(Statement sql, String consulta) throws SQLException {
        List<String> valores = new ArrayList<>();
        try (ResultSet resultado = sql.executeQuery(consulta)) {
            while (resultado.next()) {
                valores.add(resultado.getString(1));
            }
        }
        return valores;
    }
}
