package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base comum dos repositórios JDBC. Padroniza: PreparedStatement sempre com
 * parâmetros (nunca concatenação de valores), fechamento de recursos com
 * try-with-resources, tradução de erros e verificação de linhas afetadas.
 *
 * A conexão é sempre recebida por parâmetro: o repositório nunca abre nem
 * fecha conexões, nem faz commit/rollback. Quem controla a transação é o
 * {@link com.ecommerce.pedidos.repositorio.GerenciadorTransacao}.
 */
abstract class RepositorioJdbcBase {

    @FunctionalInterface
    protected interface Parametros {
        void aplicar(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    protected interface Mapeador<T> {
        T mapear(ResultSet resultado) throws SQLException;
    }

    protected <T> List<T> consultar(Connection conexao, String sql, Parametros parametros, Mapeador<T> mapeador) {
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            parametros.aplicar(statement);
            try (ResultSet resultado = statement.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (resultado.next()) {
                    lista.add(mapeador.mapear(resultado));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(e, null);
        }
    }

    protected <T> Optional<T> consultarUm(Connection conexao, String sql, Parametros parametros, Mapeador<T> mapeador) {
        List<T> lista = consultar(conexao, sql, parametros, mapeador);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    protected long contar(Connection conexao, String sql, Parametros parametros) {
        return consultarUm(conexao, sql, parametros, resultado -> resultado.getLong(1)).orElse(0L);
    }

    /** Executa um INSERT e devolve a chave gerada pelo AUTO_INCREMENT. */
    protected long inserir(Connection conexao, String sql, Parametros parametros) {
        try (PreparedStatement statement = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros.aplicar(statement);
            statement.executeUpdate();
            try (ResultSet chaves = statement.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw RepositorioException.falhaTecnica("O banco não retornou a chave gerada.", null);
                }
                return chaves.getLong(1);
            }
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(e, null);
        }
    }

    /** Executa um UPDATE/DELETE e devolve o número de linhas afetadas. */
    protected int executar(Connection conexao, String sql, Parametros parametros) {
        return executar(conexao, sql, parametros, null);
    }

    /**
     * @param mensagemFk mensagem de negócio caso o comando viole uma chave
     *                   estrangeira (erro MySQL 1451)
     */
    protected int executar(Connection conexao, String sql, Parametros parametros, String mensagemFk) {
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            parametros.aplicar(statement);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw TradutorErroSql.traduzir(e, mensagemFk);
        }
    }

    /** Objeto sem id não pode ser atualizado nem removido. */
    protected static long exigirId(Long id, String entidade) {
        if (id == null) {
            throw RepositorioException.validacao(
                entidade + " sem id não pode ser atualizado(a) ou removido(a). Cadastre o registro antes.");
        }
        if (id <= 0) {
            throw RepositorioException.validacao("Id inválido para " + entidade + ": " + id);
        }
        return id;
    }

    /**
     * Zero linhas afetadas NÃO é erro para o banco: o MySQL executou o
     * comando com sucesso, apenas nenhuma linha satisfez o WHERE. É a
     * aplicação que decide que, para ela, isso significa "o registro não
     * existe" — por isso a verificação é explícita e vira erro de domínio,
     * em vez de ser ignorada silenciosamente.
     */
    protected static void exigirLinhaAfetada(int linhasAfetadas, String entidade, Object identificador) {
        if (linhasAfetadas == 0) {
            throw RepositorioException.naoEncontrado(entidade, identificador);
        }
    }
}
