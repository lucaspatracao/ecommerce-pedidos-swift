package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.OperacaoComConexao;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transações JDBC explícitas: desliga o autocommit, executa o bloco com UMA
 * única conexão (repassada a todos os repositórios) e faz commit ou rollback.
 * A conexão é sempre fechada no final, inclusive no caminho de falha.
 */
public class JdbcGerenciadorTransacao implements GerenciadorTransacao {

    @FunctionalInterface
    public interface FonteConexao {
        Connection abrir() throws SQLException;
    }

    private final FonteConexao fonteConexao;

    public JdbcGerenciadorTransacao(FonteConexao fonteConexao) {
        this.fonteConexao = fonteConexao;
    }

    @Override
    public <T, E extends Exception> T executarEmTransacao(OperacaoComConexao<T, E> operacao) throws E {
        Connection conexao = abrir();
        try {
            iniciar(conexao);
            T resultado;
            try {
                resultado = operacao.executar(conexao);
            } catch (Throwable falha) {
                // Qualquer falha (estoque insuficiente, erro de SQL, bug) desfaz tudo.
                desfazer(conexao, falha);
                throw falha;
            }
            confirmar(conexao);
            return resultado;
        } finally {
            fechar(conexao);
        }
    }

    @Override
    public <T, E extends Exception> T executar(OperacaoComConexao<T, E> operacao) throws E {
        Connection conexao = abrir();
        try {
            return operacao.executar(conexao);
        } finally {
            fechar(conexao);
        }
    }

    private Connection abrir() {
        try {
            return fonteConexao.abrir();
        } catch (SQLException e) {
            throw RepositorioException.falhaTecnica("Não foi possível conectar ao banco de dados.", e);
        }
    }

    private void iniciar(Connection conexao) {
        try {
            conexao.setAutoCommit(false);
        } catch (SQLException e) {
            throw RepositorioException.falhaTecnica("Não foi possível iniciar a transação.", e);
        }
    }

    private void confirmar(Connection conexao) {
        try {
            conexao.commit();
        } catch (SQLException e) {
            RepositorioException erro = RepositorioException.falhaTecnica(
                "Não foi possível confirmar a transação.", e);
            desfazer(conexao, erro);
            throw erro;
        }
    }

    private void desfazer(Connection conexao, Throwable causaOriginal) {
        try {
            conexao.rollback();
        } catch (SQLException e) {
            // Não esconde a causa original: o erro do rollback vira "suprimido".
            causaOriginal.addSuppressed(e);
        }
    }

    private void fechar(Connection conexao) {
        try {
            if (!conexao.getAutoCommit()) {
                conexao.setAutoCommit(true);
            }
        } catch (SQLException ignorada) {
            // Segue para o close: a conexão será descartada de qualquer forma.
        }
        try {
            conexao.close();
        } catch (SQLException ignorada) {
            // Nada a recuperar ao descartar a conexão.
        }
    }
}
