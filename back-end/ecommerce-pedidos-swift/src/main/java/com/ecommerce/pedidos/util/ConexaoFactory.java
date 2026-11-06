package com.ecommerce.pedidos.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexaoFactory {
    private static final Properties CONFIGURACAO = carregarConfiguracao();

    private ConexaoFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                obterValor("DB_URL", "db.url"),
                obterValor("DB_USERNAME", "db.username"),
                obterValor("DB_PASSWORD", "db.password")
        );
    }

    private static Properties carregarConfiguracao() {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar o arquivo application.properties.", e);
        }
        return properties;
    }

    private static String obterValor(String variavelAmbiente, String chavePropriedade) {
        String valor = System.getenv(variavelAmbiente);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(variavelAmbiente);
        }
        if (valor == null || valor.isBlank()) {
            valor = CONFIGURACAO.getProperty(chavePropriedade);
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Configuração do banco ausente. Defina as variáveis de ambiente DB_URL, DB_USERNAME e DB_PASSWORD " +
                    "ou as propriedades db.url, db.username e db.password no application.properties."
            );
        }
        return valor;
    }
}
