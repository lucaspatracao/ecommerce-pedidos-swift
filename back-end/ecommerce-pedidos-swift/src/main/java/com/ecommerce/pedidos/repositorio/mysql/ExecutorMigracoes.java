package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplica, em ordem, os scripts versionados de {@code db/migracao} que ainda
 * não foram executados no banco, registrando cada versão na tabela
 * {@code schema_migracao}. Faz o papel de uma ferramenta como o Flyway sem
 * adicionar dependências ao projeto.
 *
 * Observação: no MySQL, comandos DDL (CREATE/ALTER) fazem commit implícito,
 * por isso os scripts não rodam dentro de uma transação.
 */
public class ExecutorMigracoes {
    static final List<String> SCRIPTS = List.of(
        "V1__esquema_inicial.sql",
        "V2__politicas_integridade_referencial.sql",
        "V3__soft_delete.sql",
        "V4__concorrencia_e_auditoria.sql"
    );

    private static final String PASTA = "db/migracao/";

    /** @return nomes dos scripts aplicados nesta execução */
    public List<String> migrar(Connection conexao) {
        List<String> aplicadas = new ArrayList<>();
        try {
            criarTabelaDeControle(conexao);
            for (String script : SCRIPTS) {
                String versao = script.substring(0, script.indexOf("__"));
                if (!jaAplicada(conexao, versao)) {
                    executarScript(conexao, script);
                    registrar(conexao, versao, script);
                    aplicadas.add(script);
                }
            }
            return aplicadas;
        } catch (SQLException e) {
            throw RepositorioException.falhaTecnica("Falha ao aplicar as migrations do banco.", e);
        }
    }

    private void criarTabelaDeControle(Connection conexao) throws SQLException {
        try (Statement statement = conexao.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS schema_migracao ("
                + "versao VARCHAR(20) NOT NULL PRIMARY KEY, "
                + "script VARCHAR(200) NOT NULL, "
                + "aplicada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private boolean jaAplicada(Connection conexao, String versao) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(
                "SELECT 1 FROM schema_migracao WHERE versao = ?")) {
            statement.setString(1, versao);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private void registrar(Connection conexao, String versao, String script) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(
                "INSERT INTO schema_migracao (versao, script) VALUES (?, ?)")) {
            statement.setString(1, versao);
            statement.setString(2, script);
            statement.executeUpdate();
        }
    }

    private void executarScript(Connection conexao, String script) throws SQLException {
        try (Statement statement = conexao.createStatement()) {
            for (String comando : separarComandos(lerScript(script))) {
                statement.execute(comando);
            }
        }
    }

    private String lerScript(String script) {
        try (InputStream entrada = ExecutorMigracoes.class.getClassLoader()
                .getResourceAsStream(PASTA + script)) {
            if (entrada == null) {
                throw new IllegalStateException("Script de migration não encontrado: " + script);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler a migration " + script, e);
        }
    }

    /** Remove comentários de linha (--) e separa os comandos por ';'. */
    static List<String> separarComandos(String conteudo) {
        StringBuilder semComentarios = new StringBuilder();
        for (String linha : conteudo.split("\\R")) {
            if (!linha.trim().startsWith("--")) {
                semComentarios.append(linha).append('\n');
            }
        }
        List<String> comandos = new ArrayList<>();
        for (String comando : semComentarios.toString().split(";")) {
            if (!comando.isBlank()) {
                comandos.add(comando.trim());
            }
        }
        return comandos;
    }
}
