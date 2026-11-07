package com.ecommerce.pedidos.repositorio.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.RepositorioException.Tipo;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Partes da camada de persistência que não precisam de MySQL. */
class PersistenciaSemBancoTest {

    @Test
    @DisplayName("tradutor converte 1451 em violação de integridade com a mensagem de negócio")
    void traduzErroDeChaveEstrangeira() {
        RepositorioException erro = TradutorErroSql.traduzir(
            new SQLException("Cannot delete or update a parent row: a foreign key constraint fails", "23000", 1451),
            "Este cliente possui pedidos e não pode ser excluído.");
        assertEquals(Tipo.VIOLACAO_INTEGRIDADE, erro.getTipo());
        assertEquals("Este cliente possui pedidos e não pode ser excluído.", erro.getMessage());

        assertEquals(Tipo.DUPLICADO, TradutorErroSql.traduzir(new SQLException("dup", "23000", 1062), null).getTipo());
        assertEquals(Tipo.FALHA_TECNICA, TradutorErroSql.traduzir(new SQLException("x", "42000", 1064), null).getTipo());
    }

    @Test
    @DisplayName("separação de comandos das migrations ignora comentários")
    void separaComandos() {
        List<String> comandos = ExecutorMigracoes.separarComandos(
            "-- comentário\nCREATE TABLE a (id INT);\n\n-- outro\nALTER TABLE a ADD b INT;\n");
        assertEquals(List.of("CREATE TABLE a (id INT)", "ALTER TABLE a ADD b INT"), comandos);
    }

    @Test
    @DisplayName("paginação calcula o offset e valida limites")
    void paginacao() {
        assertEquals(40, Pagina.de(2, 20).offset());
        assertThrows(IllegalArgumentException.class, () -> Pagina.de(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> Pagina.de(0, 0));
        assertThrows(IllegalArgumentException.class, () -> Pagina.de(0, Pagina.TAMANHO_MAXIMO + 1));
    }

    @Test
    @DisplayName("filtro de visibilidade gera a condição sobre a coluna ativo")
    void visibilidade() {
        assertEquals("p.ativo = TRUE", Visibilidade.ATIVOS.condicaoPara("p"));
        assertEquals("p.ativo = FALSE", Visibilidade.INATIVOS.condicaoPara("p"));
        assertEquals("1 = 1", Visibilidade.TODOS.condicaoPara("p"));
    }

    @Test
    @DisplayName("objeto sem id não pode ser atualizado (validação antes de tocar no banco)")
    void exigeId() {
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> RepositorioJdbcBase.exigirId(null, "Produto"));
        assertEquals(Tipo.VALIDACAO, erro.getTipo());
        assertThrows(RepositorioException.class, () -> RepositorioJdbcBase.exigirId(0L, "Produto"));
    }
}
