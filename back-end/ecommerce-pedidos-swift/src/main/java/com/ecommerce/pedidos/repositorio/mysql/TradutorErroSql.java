package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.repositorio.RepositorioException;
import java.sql.SQLException;

/**
 * Traduz erros do MySQL para {@link RepositorioException}, sem expor nome de
 * tabela, constraint ou mensagem do driver para as camadas superiores.
 */
final class TradutorErroSql {
    /** ER_ROW_IS_REFERENCED_2: excluir linha referenciada por FK (SQLSTATE 23000). */
    static final int LINHA_REFERENCIADA = 1451;
    /** ER_ROW_IS_REFERENCED (versões antigas do MySQL). */
    static final int LINHA_REFERENCIADA_LEGADO = 1217;
    /** ER_NO_REFERENCED_ROW_2: inserir filho apontando para pai inexistente. */
    static final int REFERENCIA_INEXISTENTE = 1452;
    /** ER_DUP_ENTRY: chave única duplicada. */
    static final int CHAVE_DUPLICADA = 1062;

    private TradutorErroSql() {
    }

    /**
     * @param mensagemFk mensagem de negócio usada quando o erro é violação de
     *                   chave estrangeira (ex.: "Este cliente possui pedidos...")
     */
    static RepositorioException traduzir(SQLException e, String mensagemFk) {
        int codigo = e.getErrorCode();
        if (codigo == LINHA_REFERENCIADA || codigo == LINHA_REFERENCIADA_LEGADO) {
            return RepositorioException.violacaoIntegridade(
                mensagemFk != null ? mensagemFk
                    : "O registro está em uso por outros dados e não pode ser removido.", e);
        }
        if (codigo == REFERENCIA_INEXISTENTE) {
            return RepositorioException.violacaoIntegridade(
                "O registro relacionado informado não existe.", e);
        }
        if (codigo == CHAVE_DUPLICADA) {
            return RepositorioException.duplicado(
                "Já existe um cadastro com os mesmos dados de identificação.", e);
        }
        return RepositorioException.falhaTecnica("Erro ao acessar o banco de dados.", e);
    }
}
